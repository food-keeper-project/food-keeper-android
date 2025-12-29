package com.foodkeeper.core.data.network

import io.ktor.http.HttpMethod // Ktor의 HttpMethod 사용
import kotlinx.serialization.Serializable



sealed class ApiRoute {
    // ✅ 추가: 기본값은 false로 두고, RefreshToken 클래스에서만 true로 재정의합니다.
    open val isRefreshTokenRequest: Boolean = false
    open val isLoginRequest: Boolean = false
    // 기본값을 true로 설정하고, 인증이 필요 없는 경우에만 false로 오버라이드
    //open val requiresAuth: Boolean = true
    // ========== Auth APIs ==========
    // 2. Route 정의는 데이터만 전달하는 역할만 수행
    data class KakaoLogin(
        val kakaoAccessToken: String, // 카카오에서 받은 토큰
        val mFcmToken: String?         // FCM 토큰
    ) : ApiRoute()

    data class RefreshToken(
        val curAccessToken:String,
        val curRefreshToken:String
    ) : ApiRoute()
    // ✅ MyProfile 수정: GET 요청이므로 별도의 파라미터가 필요 없습니다.
    // 인증은 requiresAuth = true를 통해 자동으로 처리됩니다.
    object MyProfile : ApiRoute()
    object Logout: ApiRoute()

    // ========== 식자재 관련 정의 ==========


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
            is MyProfile -> "api/v1/members/me" // 내 카톡 프로필 사진,이름을 가져오는 API
            is Logout -> "api/v1/auth/sign-out" // 로그아웃 api
//            is Logout -> "/auth/logout"

        }

    // ========== HTTP 메서드 정의 ==========
    val method: HttpMethod
        get() = when (this) {
            is KakaoLogin -> HttpMethod.Post
            is RefreshToken -> HttpMethod.Post
            is MyProfile -> HttpMethod.Get
            is Logout -> HttpMethod.Delete
//            is Logout -> HttpMethod.GET
        }

    // ========== 인증 필요 여부 ==========
    val requiresAuth: Boolean
        get() = when (this) {
            is KakaoLogin, is RefreshToken -> false
            else->true
        }

    // ========== Body 데이터 ==========
    val body: Any?
        get() = when (this) {
            is KakaoLogin ->  mapOf(
                "accessToken" to kakaoAccessToken,
                "fcmToken" to mFcmToken)
            is RefreshToken -> mapOf(
                "refreshToken" to curRefreshToken)
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
           is RefreshToken -> mapOf("Authorization" to "Bearer $curRefreshToken")
            else -> emptyMap()
        }

    // ========== 타임아웃 설정 (필요시) ==========
    val timeoutMillis: Long?
        get() = when (this) {
//            is exaple -> 60_000L // 파일 업로드 등 시간이 오래 걸리는 작업
            else -> null // 기본값 사용
        }
}