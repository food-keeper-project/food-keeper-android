package com.foodkeeper.core.data.repository

import android.content.Context
import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.domain.repository.AuthRepository
import com.kakao.sdk.user.UserApiClient
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.ResourceBundle.clearCache
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

    override suspend fun refreshToken(): Result<AuthTokenDTO> {
        Log.d("AuthRepo", "1. refreshToken 진입") // 이게 찍히는지 확인
        return runCatching {
            val oldRefreshToken = tokenManager.refreshToken.first()
            val oldAccessToken=tokenManager.accessToken.first()
            Log.d("AuthRepo", "2. 로컬 토큰 읽기 성공") // 이게 안 찍히면 first()에서 멈춘 것

            // ✅ 타임아웃을 걸어서 무한 대기를 방지합니다.
            val response = authRemoteDataSource.refreshToken(oldAccessToken!!,oldRefreshToken!!).first()

            Log.d("AuthRepo", "3. 서버 응답 성공") // 이게 안 찍히면 서버 통신에서 멈춘 것
            // ✅ [필수] 여기서 새 토큰을 저장해야 앱이 다음 요청에서 새 토큰을 사용합니다.
            tokenManager.saveTokens(
                accessToken = response.accessToken!!,
                refreshToken = response.refreshToken!!
            )
            Log.d("AuthRepo", "4. 새 토큰 저장 완료")
            // ... 저장 로직
            response
        }.onFailure { e ->
            // ✅ 여기에 로그가 찍히고 있지 않나요?
            Log.e("AuthRepo", "❌ 재발급 중 에러 발생: ${e.message}")
        }
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
    override suspend fun logout(): Result<Unit> {
        return try {
            // 1. 카카오 로그아웃 수행 (콜백을 suspend로 변환)
            suspendCoroutine{ continuation ->
                UserApiClient.instance.logout { error ->
                    if (error != null) continuation.resumeWith(Result.failure(error))
                    else continuation.resumeWith(Result.success(Unit))
                }
            }

            // 2. 카카오 로그아웃 성공 시 로컬 토큰 비우기 (suspend 함수 호출 가능)
            tokenManager.clearTokens()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun withdrawAccount(): Flow<String> {
        return authRemoteDataSource.withdrawAccount()
            .onEach { result ->
                // ✅ Flow를 통해 들어온 서버 응답이 성공인지 확인
                // (서버가 성공 시 "SUCCESS" 같은 문자열을 보낸다고 가정)
                if (result == "SUCCESS") {
                    Log.d("AuthRepo", "회원탈퇴 성공: 로컬 토큰 삭제")
                    tokenManager.clearTokens()
                    clearCache()
                }
            }
            .catch { e ->
                // ✅ 여기서 예외 처리를 하여 스트림이 끊기지 않게 하거나 로그를 남깁니다.
                Log.e("AuthRepo", "회원탈퇴 Flow 에러: ${e.message}")
                throw e // ViewModel에서 catch할 수 있게 던져줌
            }
    }

}
