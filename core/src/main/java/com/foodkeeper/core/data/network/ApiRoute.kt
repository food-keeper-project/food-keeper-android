package com.foodkeeper.core.data.network

import io.ktor.http.HttpMethod // Ktor의 HttpMethod 사용

sealed class ApiRoute {

    // ========== Auth APIs ==========
    data class Login(
        val kakaoID: String,
    ) : ApiRoute()

    data class RefreshToken(
        val token: String
    ) : ApiRoute()

    //    data class Logout(val userId: String) : ApiRoute()
    // ========== 경로 정의 ==========
    val baseURL: String
        get() = when (this) {
            else -> ApiKey.BASE_URL
        }
    // ========== 경로 정의 ==========
    val path: String
        // TODO: URL 선언 시 앞에 '/' 제거!!
        get() = when (this) {
            // Auth
            is Login -> "api/v1" //로그인 API
            is RefreshToken -> "auth/refresh" // 엑세스 토큰 갱신 API
//            is Logout -> "/auth/logout"

        }

    // ========== HTTP 메서드 정의 ==========
    val method: HttpMethod
        get() = when (this) {
            is Login -> HttpMethod.Get
            is RefreshToken -> HttpMethod.Post
//            is Logout -> HttpMethod.GET
        }

    // ========== 인증 필요 여부 ==========
    val requiresAuth: Boolean
        get() = when (this) {
            is Login, is RefreshToken -> false
            else -> true
        }

    // ========== Body 데이터 ==========
    val body: Any?
        get() = when (this) {
            else -> null
        }

    // ========== 쿼리 파라미터 ==========
    val queryParameters: Map<String, Any>
        get() = when (this) {
            is Login -> mapOf("members" to kakaoID)
            else -> emptyMap()
        }

    // ========== 커스텀 헤더 (필요시) ==========
    val headers: Map<String, String>
        get() = when (this) {
//            is exaple -> mapOf("X-Request-Type" to "create-post")
            else -> emptyMap()
        }

    // ========== 타임아웃 설정 (필요시) ==========
    val timeoutMillis: Long?
        get() = when (this) {
//            is exaple -> 60_000L // 파일 업로드 등 시간이 오래 걸리는 작업
            else -> null // 기본값 사용
        }
}