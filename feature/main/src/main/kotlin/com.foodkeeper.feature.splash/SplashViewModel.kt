// feature/splash/src/main/kotlin/com/foodkeeper/feature/splash/SplashViewModel.kt
package com.foodkeeper.feature.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.CheckLoginStatusUseCase
import com.foodkeeper.core.domain.usecase.CheckOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Login : SplashDestination()
    object Main : SplashDestination()
    object Onboarding : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkLoginStatusUseCase: CheckLoginStatusUseCase,
    private val checkOnboardingStatusUseCase: CheckOnboardingStatusUseCase // 주입 추가

) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination = _destination.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // 1. 최소 2초 동안 스플래시 로고 노출
            val delayJob = launch { delay(2000) }

            // 2. 로그인 상태 확인 (Flow의 첫 번째 값을 가져옴)
            val isLoggedIn = checkLoginStatusUseCase().first()
            val hasSeenOnboarding = checkOnboardingStatusUseCase().first()
            // 3. 2초가 다 지날 때까지 대기
            delayJob.join()

            // 4. 목적지 결정
            _destination.value = when {
                isLoggedIn -> {
                    Log.d("Splash", "로그인 상태 -> 메인으로 이동")
                    SplashDestination.Main
                }
                !hasSeenOnboarding -> {
                    Log.d("Splash", "온보딩 미시청 -> 온보딩으로 이동")
                    SplashDestination.Onboarding
                }
                else -> {
                    Log.d("Splash", "온보딩 시청 완료/미로그인 -> 로그인으로 이동")
                    SplashDestination.Login
                }
            }
        }
    }
}
