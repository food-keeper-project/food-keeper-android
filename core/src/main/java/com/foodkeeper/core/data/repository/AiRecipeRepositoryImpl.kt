package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.datasource.external.RecipeRemoteDataSource
import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.data.mapper.external.AiRecipeListResponse
import com.foodkeeper.core.data.mapper.external.AiRecipeResponse
import com.foodkeeper.core.data.mapper.request.RecipeCreateRequest
import com.foodkeeper.core.domain.repository.AiRecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AiRecipeRepositoryImpl @Inject constructor(
    private val dataSource: RecipeRemoteDataSource
) : AiRecipeRepository {
    override suspend fun getAiRecipe(
        ingredients: List<String>,
        excludedMenus: List<String>
    ): Flow<AiRecipe> =
        dataSource.fetchAiRecipe(ingredients, excludedMenus)


    // ✅ flow { emit() } 없이 DataSource의 Flow를 그대로 반환합니다.
    override suspend fun saveRecipe(request: RecipeCreateRequest): Flow<Long> =
        dataSource.saveRecipe(request)
    override suspend fun deleteRecipe(menuId: Long): Flow<String> =
        dataSource.deleteRecipe(menuId)
    // AiRecipeRepositoryImpl.kt
    override suspend fun getSavedRecipes(cursor: Long?,
                                         limit: Int): Flow<AiRecipeListResponse> =
        dataSource.getSavedRecipes(cursor,limit)
}

