package com.foodkeeper.core.data.network

import com.foodkeeper.core.data.network.ApiRoute.RefreshToken.method
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import javax.inject.Inject

class ApiService @Inject constructor(
    val client: HttpClient
) {

    /**
     * 제네릭 API 호출 함수
     * ApiRoute를 받아서 자동으로 요청 설정
     */
    suspend inline fun <reified T> request(route: ApiRoute): T {
        return client.request(route.path) {
            method = route.method

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
}