package com.foodkeeper.feature.airecipe
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class AiRecipeUiState(
    val isLoading: Boolean = false,
    val recipeImage: String = "",
    val title: String = "",
    val description: String = "",
    val cookingTime: String = "",
    val ingredients: List<Pair<String, String>> = emptyList(),
    val steps: List<String> = emptyList(),
    val isIngredientsExpanded: Boolean = false
)
@HiltViewModel
class AiRecipeDetailViewModel @Inject constructor(
    // private val getAiRecipeUseCase: GetAiRecipeUseCase (나중에 추가)
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiRecipeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        generateRecipe() // 초기 진입 시 레시피 생성
    }

    fun generateRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: UseCase를 통해 AI 레시피 데이터 받아오기
            // 임시 더미 데이터 주입
            delay(1500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = "김치 치즈 볶음밥",
                    description = "매콤한 김치와 고소한 치즈의 환상적인 만남",
                    cookingTime = "15분",
                    ingredients = listOf("밥" to "1공기", "김치" to "100g", "모짜렐라 치즈" to "50g"),
                    steps = listOf(
                        "1. 팬에 기름을 두르고 다진 김치를 볶습니다.",
                        "2. 밥을 넣고 고루 섞으며 함께 볶습니다.",
                        "3. 치즈를 뿌리고 뚜껑을 덮어 녹입니다.",
                        "4. 김가루를 뿌려 마무리합니다."
                    )
                )
            }
        }
    }

    fun toggleIngredients() {
        _uiState.update { it.copy(isIngredientsExpanded = !it.isIngredientsExpanded) }
    }

    fun saveRecipe() {
        // TODO: 레시피 저장 로직
    }
}
