package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.datasource.external.RecipeRemoteDataSource
import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.data.mapper.external.AiRecipeListResponse
import com.foodkeeper.core.data.mapper.external.AiRecipeResponse
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.RecipeCountDTO
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
    override suspend fun deleteRecipe(menuId: Long): Flow<Unit> =
        dataSource.deleteRecipe(menuId).map {
            // ✅ 서버가 무엇을 주든(ResultDTO이든 뭐든),
            // 리포지토리가 약속한 타입인 "String"으로 변환해서 내보냅니다.
            "SUCCESS"
        }

    // AiRecipeRepositoryImpl.kt
    override suspend fun getSavedRecipes(cursor: Long?,
                                         limit: Int): Flow<AiRecipeListResponse> =
        dataSource.getSavedRecipes(cursor,limit)
    override suspend fun getSavedRecipeDetail(recipeId: Long): Flow<AiRecipe> =
        dataSource.getSavedRecipeDetail(recipeId)

    override suspend fun getMyRecipeCount(): Flow<RecipeCountDTO> =
        dataSource.getMyRecipeCount()


}

