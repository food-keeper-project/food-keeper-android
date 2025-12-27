package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.model.LoginResult
import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<LoginResult> = flow {
        // 1. 카카오 로그인 시도
        val kakaoTokenResult = authRepository.loginWithKakao()
        val kakaoToken = kakaoTokenResult.getOrElse {
            emit(LoginResult.Failure("카카오 로그인 인증 실패: ${it.message}"))
            return@flow
        }

        // 2. FCM 토큰 획득 (선택 사항일 경우 null 허용)
        val fcmToken = authRepository.getFcmToken()

        // 3. 서버 로그인 시도
        authRepository.signInWithServer(kakaoToken, fcmToken).collect { dto ->
            if (!dto.accessToken.isNullOrEmpty()) {
                // 4. 로그인 성공 시 토큰 저장
                authRepository.saveTokens(dto.accessToken, dto.refreshToken ?: "")
                emit(LoginResult.Success(dto.accessToken))
            } else {
                emit(LoginResult.Failure("서버 인증 실패: AccessToken이 없습니다."))
            }
        }
    }.catch { e ->
        // 전체 과정 중 발생하는 예상치 못한 에러 처리
        emit(LoginResult.Failure("로그인 과정 중 오류 발생: ${e.message}"))
    }
}
