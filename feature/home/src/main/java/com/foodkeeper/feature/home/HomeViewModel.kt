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
        val foodList = MutableStateFlow<List<Food>>(emptyList())
        val expiringFoodList = MutableStateFlow<List<Food>>(emptyList())

        // ----- Input: 화면 진입 -----
        input.screenEnter
            .onEach { uiState.value = BaseUiState.Loading }
            .flatMapLatest {
                combine(
                    foodUseCase.getFoodList(),
                    foodUseCase.getExpiringSoonFoodList()
                ) { allFoods, expiringFoods ->
                    allFoods to expiringFoods
                }
            }
            .onEach { (allFoods, expiringFoods) ->
                foodList.value = allFoods
                expiringFoodList.value = expiringFoods
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
            foodList = foodList.asStateFlow(),
            expiringFoodList = expiringFoodList.asStateFlow()
        )
    }

    // -------- Input / Output --------

    data class Input(
        val screenEnter: Flow<Unit>,
    )

    data class Output(
        val uiState: StateFlow<BaseUiState>,
        val foodList: StateFlow<List<Food>>,
        val expiringFoodList: StateFlow<List<Food>>
    )
}