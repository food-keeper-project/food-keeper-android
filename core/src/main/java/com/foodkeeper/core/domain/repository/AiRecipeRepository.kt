package com.foodkeeper.core.domain.repository

import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.data.mapper.external.AiRecipeListResponse
import com.foodkeeper.core.data.mapper.external.AiRecipeResponse
import com.foodkeeper.core.data.mapper.request.RecipeCreateRequest
import kotlinx.coroutines.flow.Flow

interface AiRecipeRepository {
    // ✅ Flow<Result<AiRecipe>> 형태로 반환하여 성공/실패를 스트림으로 전달
    suspend fun getAiRecipe(ingredients: List<String>, excludedMenus: List<String>): Flow<AiRecipe>
    // ... 기존 getAiRecipe 아래에 추가
    suspend fun saveRecipe(request: RecipeCreateRequest): Flow<Long>
    suspend fun deleteRecipe(menuId: Long): Flow<String>
    suspend fun getSavedRecipes(cursor: Long?,
                                limit: Int): Flow<AiRecipeListResponse>

}
