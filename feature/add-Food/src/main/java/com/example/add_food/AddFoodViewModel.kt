package com.example.add_food

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.model.AddFoodInput
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.ExpiryAlarm
import com.foodkeeper.core.domain.model.StorageMethod
import com.foodkeeper.core.domain.usecase.CategoryUseCase
import com.foodkeeper.core.domain.usecase.FoodUseCase
import com.foodkeeper.core.ui.base.BaseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodUseCase: FoodUseCase,
    private val categoryUseCase: CategoryUseCase
) : ViewModel() {

    // --------------------
    // UI State
    // --------------------

    private val _uiState = MutableStateFlow<BaseUiState>(BaseUiState.Init)
    val uiState: StateFlow<BaseUiState> = _uiState.asStateFlow()

    // 전체 카테고리 리스트
    private val _foodCategories = MutableStateFlow<List<Category>>(emptyList())
    val foodCategories: StateFlow<List<Category>> = _foodCategories.asStateFlow()

    // 전체 알림 설정 리스트
    private val _expiryAlarmList = MutableStateFlow(ExpiryAlarm.values().toList())
    val expiryAlarmList: StateFlow<List<ExpiryAlarm>> = _expiryAlarmList.asStateFlow()

    // 입력 정보 (Single Source of Truth)
    private val _foodInput = MutableStateFlow(AddFoodInput())
    val foodInput: StateFlow<AddFoodInput> = _foodInput.asStateFlow()

    // 토스트 메시지 (일회성 이벤트)
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // --------------------
    // 화면 진입
    // --------------------

    fun onScreenEnter() {
        viewModelScope.launch {
            _uiState.value = BaseUiState.Loading
            categoryUseCase.getDetailFoodCategoryList()
                .catch { e ->
                    _uiState.value = BaseUiState.ErrorState(
                        message = e.message ?: "데이터 로딩 실패"
                    )
                }
                .collect { categories ->
                    _foodCategories.value = categories
                    _uiState.value = BaseUiState.Content
                }
        }
    }

    // --------------------
    // 사용자 입력 변경
    // --------------------

    //식재료 이름
    fun updateFoodName(name: String) {
        _foodInput.update {
            it.copy(name = name)
        }
    }
    //식재료 이미지
    fun updateFoodImage(uri: Uri?) {
        _foodInput.update {
            it.copy(imageUri = uri)
        }
    }
    //카테고리
    fun updateCategory(category: Category) {
        _foodInput.update {
            it.copy(categorys = listOf(category))
        }
    }
    //보관방법
    fun updateStorageMethod(storageMethod: StorageMethod) {
        _foodInput.update {
            it.copy(storageMethod = storageMethod)
        }
    }
    //유통기한
    fun updateExpiryDate(date: Date) {
        _foodInput.update {
            it.copy(expiryDate = date)
        }
    }
    //알림
    fun updateExpiryAlarm(alarm: ExpiryAlarm) {
        _foodInput.update {
            it.copy(expiryAlarm = alarm)
        }
    }
    //메모
    fun updateMemo(memo: String) {
        _foodInput.update {
            it.copy(memo = memo)
        }
    }

    // --------------------
    // 저장
    // --------------------

    fun submitFood() {
        viewModelScope.launch {
            val input = foodInput.value

            // 간단한 validation
            if (input.name.isBlank()) {
                _toastMessage.emit("식품명을 입력해주세요")
                return@launch
            }

            if (input.expiryDate == null) {
                _toastMessage.emit("유통기한을 선택해주세요")
                return@launch
            }

            if (input.categorys.isEmpty() || input.storageMethod == null) {
                _toastMessage.emit("카테고리와 보관 방식을 선택해주세요")
                return@launch
            }

            foodUseCase.addFood(input)
                .catch {
                    _toastMessage.emit("식재료 추가 실패")
                }
                .collect { success ->
                    _toastMessage.emit("식재료가 추가되었습니다")
                }
        }
    }
}