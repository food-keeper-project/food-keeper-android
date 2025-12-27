package com.foodkeeper.core.data.network

import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ApiResponse

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Provider // Hilt에서 Lazy 주입을 위해 사용

class FoodApiService @Inject constructor(
    private val client: HttpClient,
    // 순환 참조 방지를 위해 Provider(Lazy) 사용
    private val tokenManagerProvider: Provider<TokenManager>,
    private val authRemoteDataSourceProvider: Provider<AuthRemoteDataSource>
) {
    inline fun <reified T> request(
        route: ApiRoute
    ): Flow<T> = flow {
        // 1. 첫 번째 요청 실행
        var response = executeHttpRequest(route)

        // 2. 401 Unauthorized 발생 시 재발급 로직 진입 (단, 재발급 요청 자체가 401인 경우는 제외)
        if (response.status == HttpStatusCode.Unauthorized && !route.isRefreshTokenRequest) {
            Log.d("FoodApiService", "401 발생 -> 토큰 재발급 시도")

            val isSuccess = tryRefreshToken()

            if (isSuccess) {
                Log.d("FoodApiService", "재발급 성공 -> 원래 요청 재시도")
                // 새 토큰으로 다시 요청 (이때 route.headers는 새 토큰을 반영해야 함)
                // 만약 ApiRoute에서 토큰을 DataStore에서 실시간으로 읽는다면 바로 재요청 가능
                response = executeHttpRequest(route)
            } else {
                Log.e("FoodApiService", "재발급 실패 -> 세션 만료 처리")
                throw UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요.")
            }
        }

        // 3. 최종 응답 처리
        val apiResponse = response.body<ApiResponse<T>>()

        if (apiResponse.result == "SUCCESS" && apiResponse.data != null) {
            emit(apiResponse.data)
        } else {
            throw ServerException(
                message = apiResponse.error?.message ?: "알 수 없는 서버 오류",
                errorCode = apiResponse.error?.errorCode
            )
        }
    }

    /**
     * 재발급 로직을 수행하는 내부 함수
     */
    @PublishedApi
    internal suspend fun tryRefreshToken(): Boolean {
        return try {
            val tokenManager = tokenManagerProvider.get()
            val authRemoteDataSource = authRemoteDataSourceProvider.get()

            // 로컬에서 리프레시 토큰 가져오기
            val oldRefreshToken = tokenManager.refreshToken.first()
            val oldAccessToken = tokenManager.accessToken.first()
            Log.d("FoodApiService", "로컬 리프레시 토큰: $oldRefreshToken")
            if (oldRefreshToken.isNullOrEmpty()) return false

            // 서버에 재발급 요청
            // 주의: authRemoteDataSource.refreshToken 내부에서 다시 request()를 호출하면 무한루프 가능성 있음
            // 따라서 재발급 API는 executeHttpRequest()를 직접 호출하거나 별도 처리 권장
            val result = authRemoteDataSource.refreshToken(oldAccessToken!!,oldRefreshToken).first()

            // 새 토큰 저장
            tokenManager.saveTokens(
                accessToken = result.accessToken!!,
                refreshToken = result.refreshToken ?: oldRefreshToken
            )
            true
        } catch (e: Exception) {
            Log.e("FoodApiService", "재발급 과정 중 예외 발생: ${e.message}")
            false
        }
    }

    @PublishedApi
    internal suspend fun executeHttpRequest(
        route: ApiRoute
    ): HttpResponse {
        return client.request(route.baseURL + route.path) {
            val tokenManager = tokenManagerProvider.get()
            val accessToken=tokenManager.accessToken.first()
            method = route.method
            contentType(ContentType.Application.Json)

            // 쿼리 및 바디 설정
            route.body?.let { setBody(it) }
            route.queryParameters.forEach { (key, value) -> parameter(key, value) }

            // 헤더 설정
            // [중요] 일반 요청 시에는 DataStore에서 최신 액세스 토큰을 가져와야 함
            // 만약 ApiRoute 내부에서 이미 처리가 되어있다면 그대로 사용
            route.headers.forEach { (key, value) -> header(key, value) }
            // ✅ 3. 일반 요청(not Refresh)이면서 토큰이 존재하는 경우 Authorization 헤더 강제 주입
            if (!route.isRefreshTokenRequest && !route.isLoginRequest) {
                header("Authorization", "Bearer $accessToken")
                Log.d("FoodApiService", "Header 주입 완료: Bearer $accessToken")
            }
            route.timeoutMillis?.let {
                timeout { requestTimeoutMillis = it }
            }
        }
    }
}

class ServerException(message: String, val errorCode: String?) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)
