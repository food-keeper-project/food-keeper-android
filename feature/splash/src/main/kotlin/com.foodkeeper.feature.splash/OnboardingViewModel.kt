package com.foodkeeper.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.SaveOnboardingStatusUseCase // 이 유스케이스를 만드셔야 합니다.
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveOnboardingStatusUseCase: SaveOnboardingStatusUseCase
) : ViewModel() {

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            saveOnboardingStatusUseCase(true)
            onComplete()
        }
    }
}
