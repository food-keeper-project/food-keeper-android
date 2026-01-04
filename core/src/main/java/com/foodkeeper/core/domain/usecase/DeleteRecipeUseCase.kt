package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.repository.AiRecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val repository: AiRecipeRepository
) {
    suspend operator fun invoke(menuId:Long):  Flow<Unit> {
        return repository.deleteRecipe(menuId)
    }
}
