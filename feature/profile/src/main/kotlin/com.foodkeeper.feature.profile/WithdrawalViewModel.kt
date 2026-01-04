package com.foodkeeper.feature.profile

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.data.network.ApiResult
import com.foodkeeper.core.domain.usecase.GetFoodCountUseCase
import com.foodkeeper.core.domain.usecase.GetSavedRecipeCountUseCase
import com.foodkeeper.core.domain.usecase.WithdrawAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val getFoodCountUseCase: GetFoodCountUseCase,
    private val getSavedRecipeCountUseCase: GetSavedRecipeCountUseCase,
    private val withdrawAccountUseCase: WithdrawAccountUseCase
) : ViewModel() {

    private val _withdrawalSuccess = MutableSharedFlow<Boolean>()
    val withdrawalSuccess = _withdrawalSuccess.asSharedFlow()
    // ✅ SharedFlow 대신 StateFlow 사용 (최신 상태 보존 및 Flow 표준 준수)
    // ✅ StateFlow 타입을 Long으로 명시하고 초기값을 0L로 설정
    private val _recipeCount = MutableStateFlow(0L)
    val recipeCount: StateFlow<Long> = _recipeCount.asStateFlow()

    private val _foodCount = MutableStateFlow(0L)
    val foodCount: StateFlow<Long> = _foodCount.asStateFlow()
    val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    init {
        Log.d("WithdrawalVM", "WithdrawalVM 생성됨")
        fetchAllCounts()
    }
    fun fetchAllCounts() {
        viewModelScope.launch {
            // 1. 레시피 개수 조회
            launch {
                getSavedRecipeCountUseCase()
                    .catch { e -> Log.e("WithdrawalVM", "레시피 조회 실패: ${e.message}") }
                    .collect { dto ->
                        // ✅ 이제 dto 자체가 데이터이므로 바로 접근
                        Log.d("WithdrawalVM", "레시피 개수 서버 응답: ${dto.recipeCount}")
                        _recipeCount.value = dto.recipeCount ?: 0L
                    }
            }

            // 2. 식재료 개수 조회
            launch {
                getFoodCountUseCase()
                    .catch { e -> Log.e("WithdrawalVM", "식자재 조회 실패: ${e.message}") }
                    .collect { dto ->
                        // ✅ 이제 dto 자체가 데이터이므로 바로 접근
                        Log.d("WithdrawalVM", "식자재 개수 서버 응답: ${dto.foodCount}")
                        _foodCount.value = dto.foodCount ?: 0L
                    }
            }
        }
    }

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
