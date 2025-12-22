package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.repository.AuthRepository
import javax.inject.Inject

class SaveOnboardingStatusUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(completed: Boolean) = repository.saveOnboardingStatus(completed)
}