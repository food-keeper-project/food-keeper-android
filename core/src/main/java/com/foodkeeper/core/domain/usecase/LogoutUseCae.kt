package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.repository.UserRepository

import javax.inject.Inject

/**
 * 로그아웃 유스케이스: 서버 로그아웃 호출 및 로컬 토큰 삭제
 */
class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager // 토큰 삭제를 위해 필요
) {
    suspend operator fun invoke(): Result<String> {
        return try {
            // 1. 서버에 로그아웃 알림
            val result = userRepository.logout()

            // 2. 서버 응답과 상관없이(또는 성공 시) 로컬 토큰 삭제
            tokenManager.clearTokens()

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
