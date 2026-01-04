package com.foodkeeper.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.data.network.ApiResult
import com.foodkeeper.core.domain.usecase.WithdrawAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val withdrawAccountUseCase: WithdrawAccountUseCase
) : ViewModel() {

    private val _withdrawalSuccess = MutableSharedFlow<Boolean>()
    val withdrawalSuccess = _withdrawalSuccess.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun withdraw() {
        viewModelScope.launch {
            withdrawAccountUseCase()
                .onStart { _isLoading.value = true }
                .collect { result ->
                    _isLoading.value = false
                    when (result) {
                        is ApiResult.Success -> {
                            // "SUCCESS" 응답 시 성공 이벤트 전송
                            _withdrawalSuccess.emit(true)
                        }
                        // ✅ 이 부분이 누락되어 에러가 발생했을 것입니다.
                        is ApiResult.Error -> {
                            val message = when (result.throwable.message) {
                                "KAKAO_UNLINK_FAIL" -> "카카오 계정 연결 해제에 실패했습니다."
                                "SERVER_FAIL" -> "서버 탈퇴 처리에 실패했습니다."
                                else -> result.throwable.message ?: "알 수 없는 오류가 발생했습니다."
                            }
                            _errorEvent.emit(message)
                        }
                    }
                }
        }
    }
}
