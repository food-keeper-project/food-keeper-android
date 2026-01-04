package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.domain.repository.FoodRepository
import com.foodkeeper.core.data.mapper.external.FoodCountDTO
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodCountUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(): Flow<FoodCountDTO> {
        return foodRepository.getFoodCount()
    }
}
