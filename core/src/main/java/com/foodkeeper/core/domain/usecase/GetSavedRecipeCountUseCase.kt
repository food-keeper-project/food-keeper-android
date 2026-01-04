// GetSavedRecipeCountUseCase.kt (도메인 레이어)
package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.RecipeCountDTO

import com.foodkeeper.core.domain.repository.AiRecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedRecipeCountUseCase @Inject constructor(
    private val recipeRepository: AiRecipeRepository
) {
    suspend operator fun invoke(): Flow<RecipeCountDTO> {
        return recipeRepository.getMyRecipeCount()
    }
}
