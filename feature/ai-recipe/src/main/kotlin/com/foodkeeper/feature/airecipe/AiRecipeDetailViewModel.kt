package com.foodkeeper.feature.airecipe
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.data.mapper.external.Ingredient
import com.foodkeeper.core.data.mapper.external.Step
import com.foodkeeper.core.data.mapper.request.IngredientRequest
import com.foodkeeper.core.data.mapper.request.RecipeCreateRequest
import com.foodkeeper.core.data.mapper.request.StepRequest
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.usecase.DeleteRecipeUseCase
import com.foodkeeper.core.domain.usecase.GetAiRecipeUseCase
import com.foodkeeper.core.domain.usecase.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class AiRecipeUiState(
    val recipeId: Long = 0L,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val recipeImage: String = "",
    val title: String = "",
    val description: String = "",
    val cookMinutes:Int = 0,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
    val isIngredientsExpanded: Boolean = false,
    val isSaved: Boolean = false
)
@HiltViewModel
class AiRecipeDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, // ✅ 1. 생성자에 추가
    private val getAiRecipeUseCase: GetAiRecipeUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiRecipeUiState())

    val uiState = _uiState.asStateFlow()

    // ✅ 지금까지 생성된 레시피 제목들을 저장할 리스트
    private val generatedTitles = mutableListOf("")


    init {
        // ✅ 네비게이션에서 넘어온 ID도 Long으로 수신
        val recipeId: Long? = savedStateHandle.get<Long>("recipeId")
        recipeId?.let {
            if (it != 0L) {
                _uiState.update { state -> state.copy(recipeId = it) }
                loadSavedRecipe(it)
            }
        }
        Log.d("AiRecipeDetailViewModel", "recipeId: $recipeId")
    }
    override fun onCleared() {
        super.onCleared()
        clearAllState() // ViewModel이 파괴될 때 상태 초기화
    }

    fun clearAllState() {
        _uiState.update { AiRecipeUiState() } // 아예 새로 생성된 초기 상태로 리셋
    }
    // [통합 버튼 클릭 이벤트]
    fun toggleSaveState() {
        if (_uiState.value.isSaved) {
            deleteRecipe() // 이미 저장됨 -> 삭제 호출
        } else {
            saveRecipe()   // 저장 안됨 -> 저장 호출
        }
    }

    // [삭제 로직]
    private fun deleteRecipe() {
        if (_uiState.value.isSaving || _uiState.value.isLoading) return
        Log.d("AiRecipeDetail", "삭제 시도 - 현재 ID: ${_uiState.value.recipeId}") // 추가

        val currentId = _uiState.value.recipeId
        if (currentId == 0L) return // ✅ 0L 체크

        viewModelScope.launch {
            deleteRecipeUseCase(currentId) // ✅ 이제 이름을 보냈던 자리에 ID를 보냄
                .onStart { _uiState.update { it.copy(isSaving = true) } }
                .catch { e -> _uiState.update { it.copy(isSaving = false) }
                    Log.e("AiRecipeDetail", "삭제 실패 에러 발생: ${e.message}")}
                .collect {
                    _uiState.update { it.copy(isSaving = false, recipeId = 0L, isSaved = false ) } // ID 비우기
                    Log.d("AiRecipeDetail", "삭제 성공 - 하트 해제")
                }
        }
    }
    fun loadSavedRecipe(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: UseCase를 통해 ID에 해당하는 레시피 정보를 DB/서버에서 가져옴
            // 예시: val recipe = getAiRecipeDetailUseCase(id)
            _uiState.update { it.copy(isLoading = false, title = "불러온 레시피...") }
        }
    }

    fun fetchRecipeDetail() {
        viewModelScope.launch {}
    }

    fun generateRecipe(food: List<Food>) {
        // ✅ 1. 새 레시피 생성 시작 시 모든 데이터 필드를 빈 값으로 즉시 리셋
        _uiState.update {
            it.copy(
                isSaved = false,
                recipeId = 0L,
                isLoading = true, // 로딩을 즉시 true로
                title = "",       // 이전 제목 제거
                description = "", // 이전 설명 제거
                ingredients = emptyList(), // 이전 재료 제거
                steps = emptyList(),       // 이전 순서 제거
                recipeImage = ""           // 이전 이미지 제거
            )
        }

        // 2. 이미 저장 중이면 무시 (isLoading은 위에서 이미 처리함)
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            val ingredientNames = food.map { it.name }

            // ✅ 2. UseCase 호출 (Flow<AiRecipe> 처리)
            getAiRecipeUseCase(ingredientNames, generatedTitles)
                .onStart {
                    // 시작 시 로딩 표시
                    _uiState.update { it.copy(isLoading = true) }
                }
                .catch { exception ->
                    // ✅ 3. 에러 발생 시 처리 (Flow<T>이므로 catch 사용)
                    _uiState.update { it.copy(isLoading = false) }
                    Log.e("AiRecipeDetail", "레시피 생성 실패: ${exception.message}")
                }
                .collect { recipe ->
                    // ✅ 4. 데이터 수신 성공 시 처리
                    if (!generatedTitles.contains(recipe.title ?: "")) {
                        recipe.title?.let { generatedTitles.add(it) }
                    }
                    Log.d("TAG", "generateRecipe: $generatedTitles")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = recipe.title ?: "",
                            description = recipe.description ?: "",
                            cookMinutes = recipe.cookMinutes ?: 0,
                            ingredients = recipe.ingredients ?: emptyList(),
                            steps = recipe.steps ?: emptyList(),
                            recipeImage = recipe.recipeImage ?: "" // 이미지 경로 추가
                        )
                    }
                }
        }
    }


    fun toggleIngredients() {
        _uiState.update { it.copy(isIngredientsExpanded = !it.isIngredientsExpanded) }
    }

    fun saveRecipe() {
        // 1. 이미 저장 중이거나 로딩 중이면 무시 (가드 로직)
        if (_uiState.value.isSaving || _uiState.value.isLoading) return

        val currentState = _uiState.value

        // 1. 서버 스펙에 맞는 요청 DTO 생성
        val request = RecipeCreateRequest(
            menuName = currentState.title,
            description = currentState.description,
            cookMinutes = currentState.cookMinutes,
            ingredients = currentState.ingredients.map {
                IngredientRequest(name = it.name ?: "", quantity = it.quantity ?: "")
            },
            steps = currentState.steps.map {
                StepRequest(title = it.title ?: "", content = it.content ?: "")
            }
        )

        viewModelScope.launch {
            saveRecipeUseCase(request)
                .onStart {
                    // ✅ isLoading이 아닌 isSaving을 true로!
                    _uiState.update { it.copy(isSaving = true) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isSaving = false) }
                }
                .collect { recipeId -> // ✅ 여기서 추출된 ID를 받음
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            recipeId = recipeId ,// ✅ UI 상태에 저장
                            isSaved = true
                        )
                    }
                    Log.d("AiRecipeDetail", "저장 성공! 생성된 ID: $recipeId")
                }
        }
    }
}