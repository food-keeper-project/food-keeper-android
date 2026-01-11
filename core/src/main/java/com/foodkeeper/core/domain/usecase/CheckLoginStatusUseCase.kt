// core/src/main/kotlin/com/com.swyp.com.swyp.com.swyp.kitchenlog/core/domain/usecase/CheckLoginStatusUseCase.kt
package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckLoginStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    // invoke 연산자를 사용하면 함수처럼 호출 가능: useCase()
    operator fun invoke(): Flow<Boolean> {
        return authRepository.hasToken()
    }
}
