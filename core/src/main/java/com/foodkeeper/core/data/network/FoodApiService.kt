package com.foodkeeper.core.data.network

import android.util.Log
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.error
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.ToKenDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod

import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FoodApiService @Inject constructor(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {

    inline fun <reified T> request(route: ApiRoute): Flow<T> = flow {
        val response = internalRequest(route)
        // 1. 서버 전체 응답을 ApiResponse<T> 형태로 파싱
        val apiResponse = response.body<ApiResponse<T>>()

        // 2. 서버에서 정의한 비즈니스 로직 성공 여부 확인
        if (apiResponse.result == "SUCCESS" && apiResponse.data != null) {
            emit(apiResponse.data)
            Log.d("TAG", "request: apiResponse.data : ${apiResponse.data}")
        } else {
            // 실패 시 서버가 보내준 에러 메시지를 예외로 던짐
            throw ServerException(
                message = apiResponse.error?.message ?: "알 수 없는 서버 오류",
                errorCode = apiResponse.error?.errorCode
            )
        }
    }

    /**
     * 토큰 갱신을 포함한 모든 내부 로직을 처리하는 핵심 함수.
     * Non-inline이며, 외부에는 노출되지 않는다.
     * 결과로 HttpResponse를 반환한다.
     */
    @PublishedApi
    internal suspend fun internalRequest(route: ApiRoute): HttpResponse {
        return try {
            // 실제 요청을 실행하고 HttpResponse를 그대로 반환
            executeHttpRequest(route)
        } catch (e: ClientRequestException) {

            if (e.response.status == HttpStatusCode.Unauthorized && route.requiresAuth) {
                // 토큰 갱신 및 재시도 로직 호출
                refreshTokenAndRetry(route)
            } else {
                throw e
            }
        }
    }

    /**
     * 토큰 갱신 후 원래 요청을 재시도하는 함수.
     * Non-inline이며, 결과로 HttpResponse를 반환한다.
     */
    private suspend fun refreshTokenAndRetry(route: ApiRoute): HttpResponse {
        // 1. 로컬에 저장된 리프레시 토큰 가져오기
        val storedRefreshToken = tokenManager.getRefreshToken()
            ?: throw UnauthorizedException("리프레시 토큰이 없습니다.")

        return try {
            // 2. 토큰 재발급 API 호출
            // executeHttpRequest를 직접 쓰지 않고 여기서 별도로 구성하거나
            // ApiRoute에 파라미터 로직을 포함시켜야 합니다.

            val response = client.request {
                url {
                    path("api/v1/auth/refresh") // 갱신 엔드포인트
                    // ✅ 서버 요구대로 파라미터에 refreshToken 추가
                    parameter("refreshToken", storedRefreshToken)
                }
                method = HttpMethod.Post
            }

            val apiResponse = response.body<ApiResponse<AuthTokenDTO>>()

            if (apiResponse.result == "SUCCESS" && apiResponse.data != null) {
                val newData = apiResponse.data
                // 3. 새 토큰들 저장
                tokenManager.saveTokens(
                    accessToken = newData.accessToken ?: "",
                    refreshToken = newData.refreshToken ?: ""
                )

                // 4. 원래 실패했던 요청 재시도 (새 토큰 적용됨)
                executeHttpRequest(route)
            } else {
                throw UnauthorizedException("토큰 갱신 응답 실패")
            }
        } catch (e: Exception) {
            tokenManager.clearTokens() // 실패 시 토큰 비우고 로그아웃 처리 유도
            throw UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요.")
        }
    }


    /**
     * 순수하게 HTTP 요청만 실행하는 가장 내부 함수.
     * Non-inline이며, 결과로 HttpResponse를 반환한다.
     */
    private suspend fun executeHttpRequest(route: ApiRoute): HttpResponse {
        return client.request(route.baseURL + route.path) {
            method = route.method
            // 1. 헤더에 JSON 전송임을 명시 (매우 중요)
            contentType(ContentType.Application.Json)

            // 1. 만약 RefreshToken 루트라면 파라미터 주입
            if (route is ApiRoute.RefreshToken) {
                url {
                    parameter("refreshToken", route.curRefreshToken)
                }
            }

            // 2. 일반 인증이 필요한 경우 AccessToken 주입
            if (route.requiresAuth) {
                val token = tokenManager.getAccessToken()
                header("Authorization", "Bearer $token")
            }

            route.body?.let {
                setBody(it)
                Log.d("TAG", "executeHttpRequest: body $it")
            }
            route.queryParameters.forEach { (key, value) -> parameter(key, value) }
            route.headers.forEach { (key, value) -> header(key, value) }
            route.timeoutMillis?.let {
                timeout { requestTimeoutMillis = it }
            }
        } // .body()를 여기서 호출하지 않고 HttpResponse 자체를 반환
    }
}

// Custom Exception

class ServerException(message: String, val errorCode: String?) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)