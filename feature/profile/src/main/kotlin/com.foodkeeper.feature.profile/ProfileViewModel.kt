package com.foodkeeper.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    // 별도의 UiState 클래스 없이 ProfileDTO를 바로 사용 (초기값은 빈 값)
    private val _userProfile = MutableStateFlow<ProfileDTO?>(null)
    val userProfile = _userProfile.asStateFlow()

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
        // 로그아웃 로직 (토큰 삭제 및 캐시 초기화 호출 등)
    }
}
