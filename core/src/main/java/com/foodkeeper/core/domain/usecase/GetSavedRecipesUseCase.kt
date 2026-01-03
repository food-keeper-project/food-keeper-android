package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.repository.AiRecipeRepository
import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.data.mapper.external.AiRecipeResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedRecipesUseCase @Inject constructor(
    private val repository: AiRecipeRepository
) {
    suspend operator fun invoke(): Flow<List<AiRecipeResponse>> {
        return repository.getSavedRecipes()
    }
}
