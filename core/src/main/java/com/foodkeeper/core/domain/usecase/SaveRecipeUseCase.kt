package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.mapper.request.RecipeCreateRequest
import com.foodkeeper.core.domain.repository.AiRecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val repository: AiRecipeRepository
) {
    suspend operator fun invoke(request: RecipeCreateRequest): Flow<Long> {
        return repository.saveRecipe(request)
    }
}
