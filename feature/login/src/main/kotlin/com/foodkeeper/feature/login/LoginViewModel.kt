package com.foodkeeper.feature.login
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// UI가 관찰할 로그인 상태를 정의하는 sealed interface.
// sealed interface는 sealed class와 유사하지만, 상태 값 자체에 데이터가 필요 없을 때 더 간결합니다.
sealed interface LoginUiState {
    data object Idle : LoginUiState // 초기 상태
    data object Loading : LoginUiState // 로그인 진행 중
    data class Success(val token: String) : LoginUiState // 성공
    data class Error(val message: String?) : LoginUiState // 실패
}

@HiltViewModel
class LoginViewModel  @Inject constructor(

) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * 로그인을 요청하는 함수. UI(Composable)에서 이 함수를 호출하게 됩니다.
     */
    fun login(context: Context) {
        // 이미 로그인 중이면 다시 요청하지 않도록 방지
        if (_uiState.value is LoginUiState.Loading) return

        // 로그인 시작 시 상태를 Loading으로 변경하여 UI에 프로그레스 바 등을 표시하도록 알립니다.
        _uiState.value = LoginUiState.Loading

    }
}
