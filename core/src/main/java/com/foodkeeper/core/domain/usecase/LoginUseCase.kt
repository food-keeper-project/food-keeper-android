package com.foodkeeper.core.domain.usecase
import com.foodkeeper.core.domain.model.LoginResult
import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// core/src/main/kotlin/com/foodkeeper/core/domain/usecase/LoginUseCase.kt
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<LoginResult> = authRepository.login()
}
