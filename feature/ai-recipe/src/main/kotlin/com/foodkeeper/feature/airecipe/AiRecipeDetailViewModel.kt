package com.foodkeeper.feature.airecipe
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
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
    val isIngredientsExpanded: Boolean = false,
    var isSaved: MutableState<Boolean> = mutableStateOf(false)
)
@HiltViewModel
class AiRecipeDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle // ✅ 1. 생성자에 추가
    // private val getAiRecipeUseCase: GetAiRecipeUseCase (나중에 추가)
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiRecipeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // ✅ 네비게이션에서 넘어온 recipeId가 있는지 확인
        val recipeId: String? = savedStateHandle["recipeId"]
        Log.d("AiRecipeDetailViewModel", "recipeId: $recipeId")

        generateRecipe()
    }
    fun loadSavedRecipe(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: UseCase를 통해 ID에 해당하는 레시피 정보를 DB/서버에서 가져옴
            // 예시: val recipe = getAiRecipeDetailUseCase(id)
            _uiState.update { it.copy(isLoading = false, title = "불러온 레시피...") }
        }
    }
    fun generateRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: UseCase를 통해 AI 레시피 데이터 받아오기
            // 임시 더미 데이터 주입
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
