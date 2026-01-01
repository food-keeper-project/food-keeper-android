package com.foodkeeper.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.usecase.CategoryUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.foodkeeper.core.domain.usecase.FoodUseCase
import com.foodkeeper.core.ui.base.BaseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodUseCase: FoodUseCase,
    private val categoryUseCase: CategoryUseCase
) : ViewModel() {

    // --------------------
    // UI State
    // --------------------

    private val _uiState = MutableStateFlow<BaseUiState>(BaseUiState.Init)
    val uiState: StateFlow<BaseUiState> = _uiState.asStateFlow()

    private val _expiringFoodList = MutableStateFlow<List<Food>>(emptyList())
    val expiringFoodList: StateFlow<List<Food>> = _expiringFoodList.asStateFlow()

    private val _foodCategories = MutableStateFlow<List<String>>(emptyList())
    val foodCategories: StateFlow<List<String>> = _foodCategories.asStateFlow()

    private val _foodList = MutableStateFlow<List<Food>>(emptyList())
    val foodList: StateFlow<List<Food>> = _foodList.asStateFlow()

    private val _selectedFood = MutableStateFlow<Food?>(null)
    val selectedFood: StateFlow<Food?> = _selectedFood.asStateFlow()

    private val _selectedRecipeRecommend = MutableStateFlow<List<Food>?>(null)
    val selectedRecipeRecommend: StateFlow<List<Food>?> = _selectedRecipeRecommend.asStateFlow()

    // 토스트 메시지를 위한 일회성 이벤트 Flow
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // --------------------
    // 화면 진입
    // --------------------

    fun onScreenEnter() {
        viewModelScope.launch {
            _uiState.value = BaseUiState.Loading
            combine(
                foodUseCase.getFoodList(),
                foodUseCase.getExpiringSoonFoodList(),
                categoryUseCase.getFoodCategoryList()
            ) { allFoods, expiringFoods, categories ->
                Triple(allFoods, expiringFoods, categories)
            }
                .catch { e ->
                    _uiState.value = BaseUiState.ErrorState(
                        message = e.message ?: "데이터 로딩 실패"
                    )
                }
                .collect { (allFoods, expiringFoods, categories) ->
                    _foodList.value = allFoods
                    _expiringFoodList.value = expiringFoods
                    _foodCategories.value = categories
                    _uiState.value = BaseUiState.Content
                }
        }
    }

    // --------------------
    // 사용자 액션
    // --------------------

    fun onFoodItemClick(food: Food) {
        viewModelScope.launch {
            _selectedFood.value = food
        }
    }

    fun onDismissDialog() {
        viewModelScope.launch {
            _selectedFood.value = null
            _selectedRecipeRecommend.value = null
        }
    }

    fun onConsumptionFood(food: Food) {
        viewModelScope.launch {
            foodUseCase.ConsumptionFood(food)
                .catch {
                    _toastMessage.emit("다시 시도해 주세요.")
                }
                .collect { success ->
                    if (success) {
                        _selectedFood.value = null
                        _uiState.value = BaseUiState.Processing
                        onScreenEnter()
                        _toastMessage.emit("${food.name}을(를) 소비했습니다.")
                        _uiState.value = BaseUiState.Content
                    } else {
                        _toastMessage.emit("다시 시도해 주세요.")
                    }
                }

        }
    }
    fun onRecipeRecommendClick(foodList: List<Food>) {
        viewModelScope.launch {
            _selectedRecipeRecommend.value = foodList
        }
    }
}