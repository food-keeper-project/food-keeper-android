package com.foodkeeper.core.data.datasource.remote

import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.datasource.mapper.External.ToKenDTO
import com.foodkeeper.core.data.datasource.remote.network.ApiRoute
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class FoodApiService @Inject constructor(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {

    /**
     * 제네릭 API 호출 함수
     * ApiRoute를 받아서 자동으로 요청 설정 + 인증 처리
     */
    suspend inline fun <reified T> request(route: ApiRoute): T {
        return try {
            executeRequest(route)
        } catch (e: ClientRequestException) {
            // 401 Unauthorized 발생 시 토큰 갱신 후 재시도
            if (e.response.status == HttpStatusCode.Unauthorized && route.requiresAuth) {
                refreshTokenAndRetry(route)
            } else {
                throw e
            }
        }
    }

    /**
     * 실제 HTTP 요청 실행
     */
    @PublishedApi
    internal suspend inline fun <reified T> executeRequest(route: ApiRoute): T {
        return client.request(route.path) {
            method = route.method

            // 인증이 필요한 경우 토큰 추가
            if (route.requiresAuth) {
                val accessToken = tokenManager.getAccessToken()
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                } else {
                    throw UnauthorizedException("액세스 토큰이 없습니다")
                }
            } else if (route is ApiRoute.RefreshToken) {
                header("Authorization", "Bearer ${route.token}")
            }

            // Body 설정
            route.body?.let { setBody(it) }

            // 쿼리 파라미터 설정
            route.queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }

            // 커스텀 헤더 설정
            route.headers.forEach { (key, value) ->
                header(key, value)
            }

            // 타임아웃 설정
            route.timeoutMillis?.let {
                timeout {
                    requestTimeoutMillis = it
                }
            }
        }.body()
    }

    /**
     * 토큰 갱신 후 원래 요청 재시도
     */
    @PublishedApi
    internal suspend inline fun <reified T> refreshTokenAndRetry(route: ApiRoute): T {
        // 1. 리프레시 토큰 확인
        val refreshToken = tokenManager.getRefreshToken()
            ?: throw UnauthorizedException("리프레시 토큰이 없습니다. 다시 로그인해주세요.")

        try {
            // 2. 토큰 갱신 API 호출
            val refreshResponse = executeRequest<ToKenDTO>(ApiRoute.RefreshToken(refreshToken))

            // 3. 새로운 토큰 저장
            tokenManager.saveTokens(
                accessToken = refreshResponse.accessToken,
                refreshToken = refreshResponse.refreshToken
            )

            // 4. 원래 요청 재시도
            return executeRequest(route)

        } catch (e: Exception) {
            // 토큰 갱신 실패 시 로그아웃 처리
            tokenManager.clearTokens()
            throw UnauthorizedException("토큰 갱신에 실패했습니다. 다시 로그인해주세요.")
        }
    }
}

// Custom Exception
class UnauthorizedException(message: String) : Exception(message)
