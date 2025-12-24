package com.foodkeeper.core.data.repository

import android.content.Context
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.domain.repository.AuthRepository
import com.kakao.sdk.user.UserApiClient
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val tokenManager: TokenManager
) : AuthRepository {

    // ✅ [온보딩 상태 저장]
    // 이미 tokenManager에 로직이 있으므로 호출만 하면 끝입니다.
    override suspend fun saveOnboardingStatus(completed: Boolean) {
        tokenManager.saveOnboardingCompleted(completed)
    }

    // ✅ [온보딩 여부 확인]
    // 더미 context.dataStore 대신 주입받은 tokenManager를 사용합니다.
    override fun hasSeenOnboarding(): Flow<Boolean> {
        return tokenManager.hasSeenOnboarding
    }

    // ✅ [로그인 토큰 존재 여부 확인]
    // 더미 emit(false) 대신 실제 저장된 accessToken이 비어있지 않은지 확인합니다.
    override fun hasToken(): Flow<Boolean> {
        return tokenManager.accessToken.map { token ->
            !token.isNullOrBlank()
        }
    }

    // ✅ [카카오 로그인] (완성본)
    override suspend fun loginWithKakao(): Result<String> = suspendCoroutine { continuation ->
        val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                continuation.resume(Result.failure(error))
            } else if (token != null) {
                continuation.resume(Result.success(token.accessToken))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    // ✅ [FCM 토큰 획득]
    override suspend fun getFcmToken(): String? = runCatching {
        FirebaseMessaging.getInstance().token.await()
    }.getOrNull()

    // ✅ [서버 로그인]
    override fun signInWithServer(kakaoToken: String, fcmToken: String?): Flow<AuthTokenDTO> {
        return authRemoteDataSource.signInWithKakao(kakaoToken, fcmToken)
    }

    // ✅ [토큰 저장]
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.saveTokens(accessToken, refreshToken)
    }

    // ✅ [로그아웃]
    override suspend fun logout(): Result<Unit> = suspendCoroutine { continuation ->
        UserApiClient.instance.logout { error ->
            if (error != null) continuation.resume(Result.failure(error))
            else {
                // 카카오 로그아웃 성공 시 로컬 토큰도 함께 비워주는 것이 정석입니다.
                // tokenManager.clearAll() // 만약 clear 기능이 있다면 추가하세요.
                continuation.resume(Result.success(Unit))
            }
        }
    }
}
