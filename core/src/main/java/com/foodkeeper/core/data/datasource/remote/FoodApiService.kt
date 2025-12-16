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
import io.ktor.client.statement.HttpResponse

import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class FoodApiService @Inject constructor(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {

    /**
     * 최종 사용자(ViewModel 등)가 호출할 공개 API.
     * inline을 유지하여 reified T의 장점을 살린다.
     */
    suspend inline fun <reified T> request(route: ApiRoute): T {
        // 1. 내부 구현체를 호출하고, 결과(HttpResponse)를 받는다.
        val response = internalRequest(route)

        // 2. 받은 응답을 원하는 타입 T로 변환하여 반환한다. (reified의 역할)
        return response.body()
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
        val refreshToken = tokenManager.getRefreshToken()
            ?: throw UnauthorizedException("리프레시 토큰이 없습니다. 다시 로그인해주세요.")

        try {
            // 토큰 갱신 API 호출 (결과로 HttpResponse를 받음)
            val refreshResponse = executeHttpRequest(ApiRoute.RefreshToken(refreshToken))
            val tokenDto: ToKenDTO = refreshResponse.body() // 수동으로 타입 변환

            // 새로운 토큰 저장
            tokenManager.saveTokens(
                accessToken = tokenDto.accessToken,
                refreshToken = tokenDto.refreshToken
            )

            // 원래 요청 재시도
            return executeHttpRequest(route)

        } catch (e: Exception) {
            tokenManager.clearTokens()
            throw UnauthorizedException("토큰 갱신에 실패했습니다. 다시 로그인해주세요.")
        }
    }

    /**
     * 순수하게 HTTP 요청만 실행하는 가장 내부 함수.
     * Non-inline이며, 결과로 HttpResponse를 반환한다.
     */
    private suspend fun executeHttpRequest(route: ApiRoute): HttpResponse {
        return client.request(route.path) {
            method = route.method

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

            route.body?.let { setBody(it) }
            route.queryParameters.forEach { (key, value) -> parameter(key, value) }
            route.headers.forEach { (key, value) -> header(key, value) }
            route.timeoutMillis?.let {
                timeout { requestTimeoutMillis = it }
            }
        } // .body()를 여기서 호출하지 않고 HttpResponse 자체를 반환
    }
}

// Custom Exception
class UnauthorizedException(message: String) : Exception(message)