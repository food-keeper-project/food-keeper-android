package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.repository.UserRepository
import com.foodkeeper.core.domain.repository.AuthRepository

import javax.inject.Inject

/**
 * 로그아웃 유스케이스: 서버 로그아웃 호출 및 로컬 토큰 삭제
 */
class LogoutUseCase @Inject constructor(

    private val userRepository: UserRepository

) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 1. 서버에 로그아웃 알림
            val result = userRepository.logout()

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
