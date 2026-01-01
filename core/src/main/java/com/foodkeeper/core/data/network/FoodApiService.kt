package com.foodkeeper.core.data.network

import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.ResultDTO

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
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Provider // Hilt에서 Lazy 주입을 위해 사용

class FoodApiService @Inject constructor(
    private val client: HttpClient,
    @PublishedApi internal val tokenManagerProvider: Provider<TokenManager>,
    @PublishedApi internal val authRemoteDataSourceProvider: Provider<AuthRemoteDataSource>
) {
    inline fun <reified T> request(
        route: ApiRoute
    ): Flow<T> = flow {
        // 1. 첫 번째 요청 실행
        var response = executeHttpRequest(route)

        // 💡 응답 바디를 미리 역직렬화하여 에러 코드를 확인
        val initialApiResponse = response.body<ApiResponse<T>>()

        // 2. 401 Unauthorized 또는 에러 코드가 E3003일 때 재발급 로직 진입
        val isExpired = response.status == HttpStatusCode.Unauthorized ||
                initialApiResponse.error?.errorCode == "E3003"

        if (isExpired && !route.isRefreshTokenRequest) {
            Log.d("FoodApiService", "토큰 만료 감지 (E3003) -> 재발급 시도")

            val isSuccess = tryRefreshToken()

            if (isSuccess) {
                Log.d("FoodApiService", "재발급 성공 -> 원래 요청 재시도")
                response = executeHttpRequest(route)

                val retryApiResponse = response.body<ApiResponse<T>>()

                // ✨ 201 Created 또는 data가 있는 경우 성공 처리
                if (retryApiResponse.result == "SUCCESS") {
                    handleSuccessResponse(retryApiResponse, response.status)
                } else {
                    throw ServerException(
                        message = retryApiResponse.error?.message ?: "알 수 없는 서버 오류",
                        errorCode = retryApiResponse.error?.errorCode
                    )
                }
            } else {
                Log.e("FoodApiService", "재발급 실패 (리프레시 토큰 만료) -> 로그인 필요")
                tokenManagerProvider.get().clearTokens()
                SessionManager.emitLogout()
            }
        } else {
            // 3. 만료 상황이 아니면 첫 번째 결과를 그대로 처리
            if (initialApiResponse.result == "SUCCESS") {
                // ✨ 201 Created 또는 data가 있는 경우 성공 처리
                handleSuccessResponse(initialApiResponse, response.status)
            } else {
                throw ServerException(
                    message = initialApiResponse.error?.message ?: "알 수 없는 서버 오류",
                    errorCode = initialApiResponse.error?.errorCode
                )
            }
        }
    }

    /**
     * ✨ 성공 응답 처리 헬퍼 함수
     * 201 Created 또는 data가 있는 경우를 처리
     */
    @PublishedApi
    internal suspend inline fun <reified T> FlowCollector<T>.handleSuccessResponse(
        apiResponse: ApiResponse<T>,
        httpStatus: HttpStatusCode
    ) {
        when {
            // ✅ Case 1: 201 Created - data가 null이어도 성공
            httpStatus == HttpStatusCode.Created -> {
                Log.d("FoodApiService", "201 Created 응답 - data 없이 성공 처리")

                // T 타입이 Unit이면 Unit 반환, 아니면 SuccessResponse 반환
                @Suppress("UNCHECKED_CAST")
                val result = when (T::class) {
                    Unit::class -> Unit as T
                    else -> ResultDTO(result = "SUCCESS") as T
                }
                emit(result)
            }

            // ✅ Case 2: 200 OK with data
            apiResponse.data != null -> {
                Log.d("FoodApiService", "200 OK 응답 - data 포함")
                emit(apiResponse.data)
            }

            // ✅ Case 3: 200 OK without data (but SUCCESS)
            else -> {
                Log.d("FoodApiService", "200 OK 응답 - data 없음, SuccessResponse 반환")

                @Suppress("UNCHECKED_CAST")
                val result = when (T::class) {
                    Unit::class -> Unit as T
                    else -> ResultDTO(result = "SUCCESS") as T
                }
                emit(result)
            }
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

            val oldRefreshToken = tokenManager.refreshToken.first()
            val oldAccessToken = tokenManager.accessToken.first()

            Log.d("FoodApiService", "로컬 리프레시 토큰: $oldRefreshToken")

            if (oldRefreshToken.isNullOrEmpty()) return false

            // ✅ request() 대신 executeHttpRequest()를 직접 호출
            val response = executeHttpRequest(
                ApiRoute.RefreshToken(oldAccessToken!!, oldRefreshToken)
            )
            val result = response.body<ApiResponse<AuthTokenDTO>>()

            if (result.result == "SUCCESS" && result.data != null) {
                tokenManager.saveTokens(
                    accessToken = result.data.accessToken ?: oldAccessToken,
                    refreshToken = result.data.refreshToken ?: oldRefreshToken
                )
                true
            } else {
                false
            }
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
            val accessToken = tokenManager.accessToken.first()

            method = route.method

            if (!route.multiPartRequest) {
                contentType(ContentType.Application.Json)
            }

            // 헤더 설정
            if (route.requiresAuth) {
                header("Authorization", "Bearer $accessToken")
                Log.d("FoodApiService", "Header 주입 완료: Bearer $accessToken")
            } else {
                route.headers.forEach { (key, value) -> header(key, value) }
            }

            // 쿼리 및 바디 설정
            route.body?.let { setBody(it) }
            route.queryParameters.forEach { (key, value) -> parameter(key, value) }

            route.timeoutMillis?.let {
                timeout { requestTimeoutMillis = it }
            }
        }
    }
}
class ServerException(message: String, val errorCode: String?) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)
