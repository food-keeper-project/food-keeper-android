package com.foodkeeper.feature.kakaologin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.model.LoginResult
import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

// UI가 관찰할 로그인 상태를 정의하는 sealed interface.
// sealed interface는 sealed class와 유사하지만, 상태 값 자체에 데이터가 필요 없을 때 더 간결합니다.
sealed interface LoginUiState {
    data object Idle : LoginUiState // 초기 상태
    data object Loading : LoginUiState // 로그인 진행 중
    data class Success(val token: String) : LoginUiState // 성공
    data class Error(val message: String?) : LoginUiState // 실패
}

class LoginViewModel(
    // Domain 계층의 인터페이스(:core 모듈의 AuthRepository)에만 의존합니다.
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * 로그인을 요청하는 함수. UI(Composable)에서 이 함수를 호출하게 됩니다.
     */
    fun login() {
        // 이미 로그인 중이면 다시 요청하지 않도록 방지
        if (_uiState.value is LoginUiState.Loading) return

        // 로그인 시작 시 상태를 Loading으로 변경하여 UI에 프로그레스 바 등을 표시하도록 알립니다.
        _uiState.value = LoginUiState.Loading

        authRepository.login()
            .onEach { result ->
                // :core 모듈에서 정의한 LoginResult 결과에 따라 UI 상태를 업데이트합니다.
                val newState = when (result) {
                    is LoginResult.Success -> LoginUiState.Success(result.token)
                    is LoginResult.Failure -> LoginUiState.Error(result.message)
                    // 사용자가 로그인을 취소하면 다시 초기 상태로 돌아갑니다.
                    is LoginResult.Canceled -> LoginUiState.Idle
                }
                _uiState.update { newState }
            }
            .launchIn(viewModelScope) // viewModelScope 내에서 Flow를 안전하게 수집합니다.
    }
}
