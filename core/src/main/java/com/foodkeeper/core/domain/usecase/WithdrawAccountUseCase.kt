package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WithdrawAccountUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Flow<Result<Unit>> = flow {
        repository.withdrawAccount().collect { response ->
            if (response == "SUCCESS") {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("탈퇴 처리에 실패했습니다.")))
            }
        }
    }
}