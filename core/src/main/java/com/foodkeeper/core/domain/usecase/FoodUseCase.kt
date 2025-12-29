package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.isExpiringSoon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
interface FoodUseCase {

    //사용자 등록 식재료 전체 목록 조회
    fun getFoodList(): Flow<List<Food>>

    //유통기한 임박 식품 목록 조회

    fun getExpiringSoonFoodList(): Flow<List<Food>>
     //카테고리 리스트 조회
    fun getFoodCategoryList(): Flow<List<String>>
    //식품 소비 요청
    fun ConsumptionFood(food: Food): Flow<Boolean>
}

class DefaultFoodUseCase @Inject constructor(
    // 나중에 여기에 FoodRepository 주입
) : FoodUseCase {
    //전체 식자재 리스트 조회
    override fun getFoodList(): Flow<List<Food>> = flow {
        delay(500)

        emit(
            FoodMockData.foodList
        )
    }
    //유통기한 임박 식자재 리스트 조회

    override fun getExpiringSoonFoodList(): Flow<List<Food>> {
        return getFoodList()

    }
    //카테고리 리스트 조회
    override fun getFoodCategoryList(): Flow<List<String>> {
        return flowOf(listOf("야채류", "육류", "해산물", "과일류"))

    }

    override fun ConsumptionFood(food: Food): Flow<Boolean> {
        return flow {
            delay(500)
            emit(true)
        }
    }
}