package com.foodkeeper.feature.airecipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiRecipeHistoryUiState(
    val isError:Boolean=false,
    val isLoading: Boolean = false,
    val isPaging: Boolean = false, // ✅ 추가 페이지 로딩 상태
    val savedRecipes: List<AiRecipeItemState> = emptyList(),
    val hasNext: Boolean = false,   // ✅ 서버에서 내려주는 다음 페이지 존재 여부
    val lastId: Long = 0L           // ✅ 마지막으로 불러온 레시피의 ID (다음 커서)
)

data class AiRecipeItemState(
    val id: Long,
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
        fetchSavedRecipes(isFirstPage = true)
    }

    /**
     * 레시피 목록을 불러옵니다.
     * @param isFirstPage true이면 첫 페이지(커서 0), false이면 다음 페이지(마지막 ID 기준)
     */
    fun fetchSavedRecipes(isFirstPage: Boolean = true) {
        // ❌ isPaging 중일 때 중복 방지 (하지만 첫 페이지 요청인 '새로고침'은 허용해야 함)
        if (!isFirstPage && (!uiState.value.hasNext || uiState.value.isPaging)) return

        // ✅ 만약 이미 첫 페이지 로딩 중이라면 중복 요청 방지
        if (isFirstPage && uiState.value.isLoading) return
        Log.d("AiRecipeHistory", "🚀 fetchSavedRecipes 호출됨 (isFirstPage: $isFirstPage)")

        viewModelScope.launch {

            // ✅ 첫 페이지는 0, 다음 페이지는 저장된 lastId를 커서로 사용
            val currentCursor = if (isFirstPage) null else uiState.value.lastId
            val limit = 10
            Log.d("AiRecipeHistory", "📡 API 요청 시작 (cursor: ${if (isFirstPage) 0L else uiState.value.lastId})")
            getSavedRecipesUseCase(cursor = currentCursor, limit = limit)
                .onStart {
                    _uiState.update {
                        if (isFirstPage) it.copy(isLoading = true)
                        else it.copy(isPaging = true)
                    }
                }
                .catch { e ->
                    Log.e("AiRecipeHistory", "레시피 목록 로드 실패: ${e.message}")
                    _uiState.update { it.copy(isLoading = false, isPaging = false,isError = true) }
                }
                .collect { response ->
                    // ✅ response는 서버 규격에 따라 { content: List, hasNext: Boolean } 구조라고 가정
                    // UseCase가 이 구조를 반환하도록 맞춰져 있어야 합니다.

                    val newItems = response.content.map { domain ->
                        AiRecipeItemState(
                            id = domain.id,
                            title = domain.title,
                            description = domain.description,
                            cookMinutes = domain.cookMinutes
                        )
                    }

                    _uiState.update { currentState ->
                        val updatedList = if (isFirstPage) newItems else currentState.savedRecipes + newItems
                        currentState.copy(
                            isLoading = false,
                            isPaging = false,
                            savedRecipes = updatedList,
                            hasNext = response.hasNext, // ✅ 서버 응답의 hasNext 반영
                            lastId = newItems.lastOrNull()?.id ?: currentState.lastId // ✅ 마지막 아이템의 ID를 커서로 저장
                        )
                    }
                }
        }
    }
}
