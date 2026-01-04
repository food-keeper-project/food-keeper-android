package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.network.ApiResult // ✅ ApiResult 임포트 확인
import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WithdrawAccountUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // ✅ ViewModel에서 ApiResult를 사용하므로, 반환 타입을 Flow<ApiResult<String>>으로 변경
    suspend operator fun invoke(): Flow<String> {
        return repository.withdrawAccount()
    }
}
