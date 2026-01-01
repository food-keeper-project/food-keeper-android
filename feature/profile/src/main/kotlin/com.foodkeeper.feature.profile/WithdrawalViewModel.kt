package com.foodkeeper.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.WithdrawAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    fun withdraw() {
        viewModelScope.launch {
            withdrawAccountUseCase().collect { result ->
                result.onSuccess {
                    _withdrawalSuccess.emit(true)
                }.onFailure { e ->
                    _errorEvent.emit(e.message ?: "탈퇴 처리 실패")
                }
            }
        }
    }

}
