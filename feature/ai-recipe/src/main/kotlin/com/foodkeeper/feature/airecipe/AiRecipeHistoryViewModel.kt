package com.foodkeeper.feature.airecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiRecipeHistoryUiState(
    val isLoading: Boolean = false,
    val savedRecipes: List<AiRecipeItemState> = emptyList()
)

data class AiRecipeItemState(
    val id:Long,
    val title: String,
    val description: String,
    val cookMinutes: Int
)

@HiltViewModel
class AiRecipeHistoryViewModel @Inject constructor(
    private val getSavedRecipesUseCase: GetSavedRecipesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiRecipeHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSavedRecipes()
    }

    fun loadSavedRecipes() {
        viewModelScope.launch {
            getSavedRecipesUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    // 에러 로깅
                }
                .collect { recipes ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            savedRecipes = recipes.map { domain ->
                                AiRecipeItemState(
                                    id = domain.id,
                                    title = domain.title,
                                    description = domain.description,
                                    cookMinutes = domain.cookMinutes
                                )
                            }
                        )
                    }
                }
        }
    }
}
