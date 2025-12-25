package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.isExpiringSoon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
interface FoodUseCase {

    /**
     * 사용자 등록 식재료 전체 목록 조회
     */
    fun getFoodList(): Flow<List<Food>>

    /**
     * 유통기한 임박 식품 목록 조회
     */
    fun getExpiringSoonFoodList(): Flow<List<Food>>
}

class DefaultFoodUseCase @Inject constructor(
    // 나중에 여기에 FoodRepository 주입
) : FoodUseCase {

    override fun getFoodList(): Flow<List<Food>> = flow {
        delay(500)

        emit(
            FoodMockData.foodList.map { food ->
                food.copy(
                    isExpiringSoon = food.expiryDate.isExpiringSoon()
                )
            }
        )
    }

    override fun getExpiringSoonFoodList(): Flow<List<Food>> {
        return getFoodList()
            .map { foodList ->
                foodList.filter { it.isExpiringSoon }
            }
    }
}