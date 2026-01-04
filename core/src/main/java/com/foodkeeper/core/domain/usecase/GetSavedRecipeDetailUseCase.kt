package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.mapper.external.AiRecipe
import com.foodkeeper.core.domain.repository.AiRecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 저장된 레시피의 상세 정보를 ID 기반으로 조회하는 유즈케이스
 */
class GetSavedRecipeDetailUseCase @Inject constructor(
    private val repository: AiRecipeRepository
) {
    suspend operator fun invoke(recipeId: Long): Flow<AiRecipe> {
        return repository.getSavedRecipeDetail(recipeId)
    }
}
