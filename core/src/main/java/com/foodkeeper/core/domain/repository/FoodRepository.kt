package com.foodkeeper.core.domain.repository

import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.RequestResult
import kotlinx.coroutines.flow.Flow

interface FoodRepository {

    /** 전체 식재료 조회 (페이징) */
    fun getFoodList(
        limit: Int,
        categoryId: Long?,
        cursor: Long?
    ): Flow<List<Food>>

    /** 유통기한 임박 식재료 조회 */
    fun getImminentFoodList(): Flow<List<Food>>

    /** 식재료 추가 */
    fun addFood(
        request: FoodCreateRequestDTO,
        imageBytes: ByteArray?
    ): Flow<RequestResult>

    /** 식재료 소비처리 */
    fun consumptionFood(
        foodId: Long
    ): Flow<RequestResult>
}