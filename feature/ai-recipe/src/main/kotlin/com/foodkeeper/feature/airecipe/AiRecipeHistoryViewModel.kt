package com.foodkeeper.feature.airecipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// UI에 필요한 데이터 모델
data class AiRecipeHistoryUiState(
    val isLoading: Boolean = false,
    val savedRecipes: List<AiRecipeItemState> = emptyList()
)

data class AiRecipeItemState(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String
)

@HiltViewModel
class AiRecipeHistoryViewModel @Inject constructor(
    // private val getSavedRecipesUseCase: GetSavedRecipesUseCase // 추후 구현
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiRecipeHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSavedRecipes()
    }

    fun loadSavedRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: 실제 UseCase를 통해 레시피 목록 가져오기
            // 임시 더미 데이터
            val dummyRecipes = listOf(
                AiRecipeItemState("1", "", "김치 치즈 볶음밥", "매콤 고소한 환상의 맛"),
                AiRecipeItemState("2", "", "된장 찌개", "구수한 고향의 맛"),
                AiRecipeItemState("3", "", "계란말이", "아이들이 좋아하는 영양 반찬")
            )

            _uiState.update {
                it.copy(isLoading = false, savedRecipes = dummyRecipes)
            }
        }
    }
}
