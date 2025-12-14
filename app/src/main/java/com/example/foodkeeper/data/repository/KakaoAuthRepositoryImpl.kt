package com.example.foodkeeper.data.repository

import android.content.Context
import com.foodkeeper.core.domain.model.LoginResult // 'domain'이 없는 경로로 수정
import com.foodkeeper.core.domain.repository.AuthRepository // 'domain'이 없는 경로로 수정
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// :core 모듈의 AuthRepository 인터페이스를 실제로 구현하는 클래스
class KakaoAuthRepositoryImpl @Inject constructor(
    private val context: Context // Hilt를 통해 ApplicationContext가 주입됩니다.
) : AuthRepository {

    /**
     * 카카오 로그인을 비동기적으로 처리하고 결과를 Flow로 반환합니다.
     * 카카오 SDK의 콜백 기반 API를 코루틴의 Flow로 변환합니다.
     */
    override fun login(): Flow<LoginResult> = callbackFlow {
        // 실제 로그인 로직을 수행하는 함수 호출
        val result = kakaoLogin(context)
        // 결과를 Flow로 전달
        trySend(result)
        // Flow 스트림을 닫습니다.
        close()

        // Flow가 닫힐 때까지 대기
        awaitClose { }
    }

    /**
     * 카카오 로그인을 처리하고 결과를 반환하는 suspend 함수.
     * 코루틴의 suspendCoroutine을 사용하여 콜백을 동기식 코드처럼 변환합니다.
     */
    private suspend fun kakaoLogin(context: Context): LoginResult = suspendCoroutine { continuation ->
        // 카카오톡이 설치되어 있는지 확인
        val isKakaoTalkLoginAvailable = UserApiClient.instance.isKakaoTalkLoginAvailable(context)

        if (isKakaoTalkLoginAvailable) {
            // 1. 카카오톡으로 로그인 시도
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                handleLoginResult(token, error,
                    onSuccess = { continuation.resume(LoginResult.Success(it.accessToken)) },
                    onCanceled = { continuation.resume(LoginResult.Canceled) },
                    onFailure = {
                        // 카톡 로그인 실패 시, 계정 로그인으로 재시도
                        loginWithKakaoAccount(context, continuation)
                    }
                )
            }
        } else {
            // 2. 카카오톡이 없으면 카카오계정으로 로그인 시도
            loginWithKakaoAccount(context, continuation)
        }
    }

    /**
     * 카카오계정으로 로그인하는 내부 함수
     */
    private fun loginWithKakaoAccount(
        context: Context,
        continuation: kotlin.coroutines.Continuation<LoginResult>
    ) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            handleLoginResult(token, error,
                onSuccess = { continuation.resume(LoginResult.Success(it.accessToken)) },
                onCanceled = { continuation.resume(LoginResult.Canceled) },
                onFailure = { continuation.resume(LoginResult.Failure(it?.message)) }
            )
        }
    }

    /**
     * 로그인 콜백 결과를 공통으로 처리하는 헬퍼 함수
     */
    private fun handleLoginResult(
        token: OAuthToken?,
        error: Throwable?,
        onSuccess: (OAuthToken) -> Unit,
        onCanceled: () -> Unit,
        onFailure: (Throwable?) -> Unit
    ) {
        if (error != null) {
            // 사용자가 로그인을 취소한 경우
            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                onCanceled()
            } else {
                // 그 외의 모든 에러
                onFailure(error)
            }
        } else if (token != null) {
            // 로그인 성공
            onSuccess(token)
        } else {
            // 토큰과 에러가 모두 null인 경우 (이론적으로 발생하기 어려움)
            onFailure(IllegalStateException("Kakao login failed with unknown error"))
        }
    }
}
