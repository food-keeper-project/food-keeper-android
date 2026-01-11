package com.foodkeeper.core.domain.repository

import android.content.Context
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO // 필요시 Domain용 모델로 매핑 권장
import com.foodkeeper.core.data.mapper.external.respone.AccountResponseDTO
import com.foodkeeper.core.data.mapper.request.AccountRequestDTO
import com.foodkeeper.core.data.network.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    enum class LoginType { KAKAO, EMAIL }
    suspend fun refreshToken(): Result<AuthTokenDTO>
    // 1. 로그인 상태 확인 (토큰 존재 여부)
    fun hasToken(): Flow<Boolean>

    // 2. 카카오 로그인 인증 (카카오로부터 토큰만 받아옴)
    suspend fun loginWithKakao(activityContext: Context): Result<String>

    // 3. FCM 토큰 획득
    suspend fun getFcmToken(): String?

    // 4. 서버 로그인 (백엔드 인증)
    fun signInWithServer(kakaoToken: String, fcmToken: String?): Flow<AuthTokenDTO>

    // 5. 토큰 저장
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    // 6. 로그아웃
    suspend fun logout(): Result<Unit>
    suspend fun withdrawAccount(): Flow<String>
    // ✅ 온보딩 완료 상태 저장
    suspend fun saveOnboardingStatus(completed: Boolean)

    // ✅ 온보딩 완료 여부 확인
    fun hasSeenOnboarding(): Flow<Boolean>

    suspend fun checkIdDuplicate(userId: String): Flow<AccountResponseDTO>
    suspend fun signUp(userId: String, userPw: String, email: String,nickname:String, gender:String): Flow<String>
    suspend fun verifyEmail(email: String): Flow<String>
    suspend fun verifyEmailCode(email: String, code: String):Flow<String>
    suspend fun signIn(userId: String, userPw: String, fcmToken: String?): Flow<AuthTokenDTO>

    suspend fun verifyAccount(email: String): Flow<String>
    suspend fun verifyAccountCode(email: String, code: String):Flow<String>
    suspend fun verifyPassword(email: String, account: String): Flow<String>
    suspend fun verifyPasswordCode(email: String, account: String, code: String):Flow<String>
    suspend fun resetPassword(email: String, account: String, password: String): Flow<String>
    suspend fun saveLoginType(type: LoginType)

}
