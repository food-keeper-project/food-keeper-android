package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.data.mapper.external.AiRecipeResponse
import com.foodkeeper.core.data.mapper.request.RecipeCreateRequest
import com.foodkeeper.core.data.network.FoodApiService
import com.foodkeeper.core.data.network.ApiRoute
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRemoteDataSource @Inject constructor(
    private val apiService: FoodApiService
) {
    suspend fun fetchAiRecipe(
        ingredients: List<String>,
        excludedMenus: List<String>
    ): Flow<AiRecipe> {
        return apiService.request(ApiRoute.RecommendRecipe(ingredients, excludedMenus))
    }
    suspend fun saveRecipe(request: RecipeCreateRequest): Flow<Long> {
        // apiService.request가 성공 시 성공 메시지나 ID를 반환한다고 가정
        return apiService.request(ApiRoute.PostRecipe(request))
    }
    // ... 기존 import 생략
    suspend fun deleteRecipe(menuId: Long): Flow<String> {
        // ApiRoute에 DeleteRecipe(menuName)가 정의되어 있어야 합니다.
        return apiService.request(ApiRoute.DeleteFavoriteRecipe(menuId))
    }
    // RecipeRemoteDataSource.kt
    suspend fun getSavedRecipes(): Flow<List<AiRecipeResponse>> {
        // GET 요청으로 저장된 레시피 리스트를 가져온다고 가정
        return apiService.request(ApiRoute.GetFavoriteRecipe)
    }


}
