package com.foodkeeper.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.domain.usecase.GetUserProfileUseCase
import com.foodkeeper.core.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    // 별도의 UiState 클래스 없이 ProfileDTO를 바로 사용 (초기값은 빈 값)
    private val _userProfile = MutableStateFlow<ProfileDTO?>(null)
    val userProfile = _userProfile.asStateFlow()
    private val _logoutSuccess = MutableSharedFlow<Boolean>()
    val logoutSuccess = _logoutSuccess.asSharedFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure {
                // 필요 시 에러 로그 기록
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase().onSuccess {
                _logoutSuccess.emit(true)
            }
                .onFailure {
                    // 실패해도 로컬 토큰은 지워졌으므로 로그아웃 처리 가능
                    _logoutSuccess.emit(true)
                }
        }
    }
}
