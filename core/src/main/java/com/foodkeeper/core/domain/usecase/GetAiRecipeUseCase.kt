package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.domain.repository.AiRecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAiRecipeUseCase @Inject constructor(
    private val repository: AiRecipeRepository
) {
    suspend operator fun invoke(
        ingredients: List<String>,
        excludedMenus: List<String>
    ): Flow<AiRecipe> {
        return repository.getAiRecipe(ingredients, excludedMenus)
    }
}
