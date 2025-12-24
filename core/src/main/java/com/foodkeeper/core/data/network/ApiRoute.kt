package com.foodkeeper.core.data.network

import io.ktor.http.HttpMethod // Ktor의 HttpMethod 사용
import kotlinx.serialization.Serializable



sealed class ApiRoute {

    // ========== Auth APIs ==========
    // 2. Route 정의는 데이터만 전달하는 역할만 수행
    data class KakaoLogin(
        val kakaoAccessToken: String, // 카카오에서 받은 토큰
        val mFcmToken: String?         // FCM 토큰
    ) : ApiRoute()

    data class RefreshToken(
        val curRefreshToken:String
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
            is KakaoLogin -> "api/v1/auth/sign-in/kakao" //로그인 API
            is RefreshToken -> "api/v1/auth/refresh" // 엑세스 토큰 갱신 API
//            is Logout -> "/auth/logout"

        }

    // ========== HTTP 메서드 정의 ==========
    val method: HttpMethod
        get() = when (this) {
            is KakaoLogin -> HttpMethod.Post
            is RefreshToken -> HttpMethod.Post
//            is Logout -> HttpMethod.GET
        }

    // ========== 인증 필요 여부 ==========
    val requiresAuth: Boolean
        get() = when (this) {
            is KakaoLogin, is RefreshToken -> false
            else -> true
        }

    // ========== Body 데이터 ==========
    val body: Any?
        get() = when (this) {
            is KakaoLogin ->  mapOf(
                "accessToken" to kakaoAccessToken,
                "fcmToken" to mFcmToken)
            is RefreshToken -> mapOf(
                "refreshToken" to curRefreshToken
            )
            else -> null
        }

    // ========== 쿼리 파라미터 ==========
    val queryParameters: Map<String, Any>
        get() = when (this) {

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