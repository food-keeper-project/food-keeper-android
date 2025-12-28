package com.foodkeeper.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.model.Food
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.foodkeeper.core.domain.usecase.FoodUseCase
import com.foodkeeper.core.ui.base.BaseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodUseCase: FoodUseCase
) : ViewModel() {

    fun transform(input: Input): Output {

        // ----- Output Streams -----
        val uiState = MutableStateFlow<BaseUiState>(BaseUiState.Init)
        val expiringFoodList = MutableStateFlow<List<Food>>(emptyList())
        val foodCategories = MutableStateFlow<List<String>>(emptyList())
        val foodList = MutableStateFlow<List<Food>>(emptyList())


        // ----- Input: 화면 진입 -----
        input.screenEnter
            .onEach { uiState.value = BaseUiState.Loading }
            .flatMapLatest {
                combine(
                    foodUseCase.getFoodList(),
                    foodUseCase.getExpiringSoonFoodList(),
                    foodUseCase.getFoodCategoryList() // 추가된 UseCase 호출
                ) { allFoods, expiringFoods, categories ->
                    // 데이터를 묶어서 하단으로 전달
                    Triple(allFoods, expiringFoods, categories)
                }
            }
            .onEach { (allFoods, expiringFoods, categories) ->
                foodList.value = allFoods
                expiringFoodList.value = expiringFoods
                foodCategories.value = categories // ✨ 카테고리 데이터 업데이트
                uiState.value = BaseUiState.Content
            }
            .catch { e ->
                uiState.value = BaseUiState.ErrorState(
                    message = e.message ?: "데이터 로딩 실패"
                )
            }
            .launchIn(viewModelScope)

        return Output(
            uiState = uiState.asStateFlow(),
            expiringFoodList = expiringFoodList.asStateFlow(),
            foodCategorys = foodCategories.asStateFlow(),
            foodList = foodList.asStateFlow(),

        )
    }

    // -------- Input / Output --------

    data class Input(
        val screenEnter: Flow<Unit>,
    )

    data class Output(
        val uiState: StateFlow<BaseUiState>,
        val expiringFoodList: StateFlow<List<Food>>,
        val foodCategorys: StateFlow<List<String>>,
        val foodList: StateFlow<List<Food>>,

    )
}