package com.foodkeeper.core.data.repository

import android.content.Context
import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
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
import kotlinx.coroutines.flow.collect
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
        return authRemoteDataSource.signIn(userId = userId, userPw = userPw, fcmToken = fcmToken)
            .onEach {
                // ✅ 로그인 성공 시 토큰과 함께 로그인 타입 저장
                tokenManager.saveTokens(it.accessToken!!, it.refreshToken!!)
                tokenManager.saveLoginType(TokenManager.LoginType.EMAIL) // 예시
            }
    }

    override suspend fun verifyAccount(email: String): Flow<String> {
        return authRemoteDataSource.verifyAccount(email)
    }

    override suspend fun verifyAccountCode(
        email: String,
        code: String
    ): Flow<String> {
        return authRemoteDataSource.verifyAccountCode(email,code)
    }

    override suspend fun verifyPassword(
        email: String,
        account: String
    ): Flow<String> {
        return authRemoteDataSource.verifyPassword(email,account)
    }

    override suspend fun verifyPasswordCode(
        email: String,
        account: String,
        code: String
    ): Flow<String> {
        return authRemoteDataSource.verifyPasswordCode(email,account,code)
    }

    override suspend fun resetPassword(
        email: String,
        account: String,
        password: String
    ): Flow<String> {
        return authRemoteDataSource.resetPassword(email,account,password)
    }

    override suspend fun saveLoginType(type: AuthRepository.LoginType) {        // ✅ TokenManager의 LoginType enum으로 변환하여 저장
        val tokenManagerType = when (type) {
            AuthRepository.LoginType.KAKAO -> TokenManager.LoginType.KAKAO
            AuthRepository.LoginType.EMAIL -> TokenManager.LoginType.EMAIL
        }
        tokenManager.saveLoginType(tokenManagerType)
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
            Log.d("TAG", "hasToken: $token")
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



    // ... (다른 함수들은 동일)

    override suspend fun withdrawAccount(): Flow<String> = flow {
        // 1. 서버 회원탈퇴 API를 먼저 호출합니다.
        authRemoteDataSource.withdrawAccount().collect { response ->

            // 서버 응답이 성공적인지 확인합니다.
            if (response == "SUCCESS") {

                // ✅ 2. 마지막 로그인 타입을 확인합니다.
                val lastLoginType = tokenManager.loginType.first()

                // ✅ 3. 카카오 로그인 사용자일 경우에만 연결 끊기를 수행합니다.
                if (lastLoginType == TokenManager.LoginType.KAKAO) {
                    val isKakaoUnlinkSuccess = suspendCoroutine { continuation ->
                        UserApiClient.instance.unlink { error ->
                            if (error != null) {
                                Log.e("AuthRepo", "카카오 연결 끊기 실패: ${error.message}")
                                // 연결 끊기에 실패하더라도 로컬 토큰은 삭제해야 하므로,
                                // 실패를 로깅만 하고 계속 진행하도록 true를 반환할 수도 있습니다.
                                // 여기서는 일단 엄격하게 false로 처리합니다.
                                continuation.resume(false)
                            } else {
                                Log.d("AuthRepo", "카카오 연결 끊기 성공")
                                continuation.resume(true)
                            }
                        }
                    }

                    // 카카오 연결 끊기에 실패했다면, 전체 탈퇴 프로세스를 중단합니다.
                    if (!isKakaoUnlinkSuccess) {
                        throw Exception("KAKAO_UNLINK_FAIL")
                    }
                }

                // ✅ 4. (공통) 모든 단계 성공 시 로컬 토큰을 완전히 삭제합니다.
                tokenManager.clearTokens()
                emit("SUCCESS")

            } else {
                // 서버 응답이 실패한 경우
                throw Exception("SERVER_WITHDRAW_FAIL")
            }
        }
    }.catch { e ->
        Log.e("AuthRepo", "회원탈퇴 실패: ${e.message}")
        throw e // 에러를 상위로 전파하여 ViewModel에서 처리하도록 함
    }

// ... (이하 함수들은 동일)






}
