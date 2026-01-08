package com.foodkeeper.core.data.repository

import android.content.Context
import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.respone.AccountResponseDTO
import com.foodkeeper.core.data.mapper.request.AccountRequestDTO
import com.foodkeeper.core.data.network.ApiResult
import com.foodkeeper.core.domain.repository.AuthRepository
import com.kakao.sdk.user.UserApiClient
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

    override suspend fun checkIdDuplicate(userId: String): Flow<AccountResponseDTO> {
        return authRemoteDataSource.checkIdDuplicate(userId)
    }

    override suspend fun signUp(
        userId: String,
        userPw: String,
        email: String,
        nickname: String,
        gender: String
    ): Flow<String> {
        return authRemoteDataSource.signUp(userId,userPw,email,gender,nickname)
    }

    override suspend fun verifyEmail(email: String): Flow<String> {
        return authRemoteDataSource.verifyEmail(email)
    }

    override suspend fun verifyEmailCode(
        email: String,
        code: String
    ): Flow<String> {
        return authRemoteDataSource.verifyEmailCode(email,code)
    }

    override suspend fun signIn(
        userId: String,
        userPw: String,
        fcmToken: String?
    ): Flow<AuthTokenDTO> {
        return authRemoteDataSource.signIn(userId=userId,userPw=userPw,fcmToken=fcmToken)
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

    // ✅ [카카오 로그인] (개선된 폴백 로직 적용)
    override suspend fun loginWithKakao(activityContext: Context): Result<String> = suspendCoroutine { continuation ->

        // 공통 콜백 함수
        val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("AuthRepo", "카카오 로그인 실패: ${error.message}")
                continuation.resume(Result.failure(error))
            } else if (token != null) {
                Log.d("AuthRepo", "카카오 로그인 성공: ${token.accessToken}")
                continuation.resume(Result.success(token.accessToken))
            }
        }

        // 1. 카카오톡 설치 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activityContext)) {
            UserApiClient.instance.loginWithKakaoTalk(activityContext) { token, error ->
                if (error != null) {
                    Log.e("AuthRepo", "카카오톡 로그인 시도 중 에러: ${error.message}")

                    // ✅ 사용자가 의도적으로 취소한 경우 (뒤로가기 등)
                    // 이때는 웹 로그인으로 넘기지 않고 바로 에러 처리
                    if (error is com.kakao.sdk.common.model.ClientError &&
                        error.reason == com.kakao.sdk.common.model.ClientErrorCause.Cancelled) {
                        continuation.resume(Result.failure(error))
                        return@loginWithKakaoTalk
                    }

                    // ✅ 카카오톡은 깔려있는데 로그인이 불가능한 상태라면 웹 브라우저로 재시도
                    UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
                } else if (token != null) {
                    continuation.resume(Result.success(token.accessToken))
                }
            }
        } else {
            // 2. 카카오톡이 설치되어 있지 않다면 바로 웹 브라우저로 로그인
            UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
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


    override suspend fun withdrawAccount(): Flow<String> = flow {
        // 1. 서버 회원탈퇴 API 호출
        // 여기서 collect되는 'response'는 순수 String입니다.
        authRemoteDataSource.withdrawAccount().collect { response ->

            // ✅ 서버 응답이 성공인지 확인 (보통 "SUCCESS" 또는 빈 문자열이 아님을 체크)
            if (response=="SUCCESS") {

                // 2. 카카오 연결 끊기 수행
                val isKakaoUnlinkSuccess = suspendCoroutine{ continuation ->
                    UserApiClient.instance.unlink { error ->
                        if (error != null) {
                            Log.e("AuthRepo", "카카오 연결 끊기 실패: ${error.message}")
                            continuation.resume(false)
                        } else {
                            Log.d("AuthRepo", "카카오 연결 끊기 성공")
                            continuation.resume(true)
                        }
                    }
                }

                if (isKakaoUnlinkSuccess) {
                    // 3. 모든 단계 성공 시 로컬 토큰 삭제
                    tokenManager.clearTokens()
                    emit("SUCCESS")
                } else {
                    throw Exception("KAKAO_UNLINK_FAIL")
                }
            } else {
                throw Exception("SERVER_RESPONSE_EMPTY")
            }
        }
    }.catch { e ->
        Log.e("AuthRepo", "회원탈퇴 실패: ${e.message}")
        throw e
    }






}
