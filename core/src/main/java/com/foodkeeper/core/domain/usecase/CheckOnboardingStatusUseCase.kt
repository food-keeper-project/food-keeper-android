package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckOnboardingStatusUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.hasSeenOnboarding()
}
