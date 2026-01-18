package com.foodkeeper.core.data.repository

import coil.util.CoilUtils.result
import com.foodkeeper.core.data.datasource.external.FoodRemoteDataSource
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.FoodCountDTO
import com.foodkeeper.core.data.mapper.external.toCategory
import com.foodkeeper.core.data.mapper.external.toFood
import com.foodkeeper.core.data.mapper.external.toRequestResult
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.RequestResult
import com.foodkeeper.core.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val remoteDataSource: FoodRemoteDataSource
) : FoodRepository {

    override fun getFoodList(
        limit: Int,
        categoryId: Long?,
        cursor: Long?
    ): Flow<List<Food>> {
        return remoteDataSource
            .requestFoodList(limit, categoryId, cursor)
            .map { result ->
                result.content.map { it.toFood() }
            }
    }

    override fun getImminentFoodList(): Flow<List<Food>> {
        return remoteDataSource
            .requestImminentFoodList()
            .map { result ->
                result.content.map { it.toFood() }
            }
    }

    override fun addFood(
        request: FoodCreateRequestDTO,
        imageBytes: ByteArray?
    ): Flow<RequestResult> {
        return remoteDataSource
            .requestAddFood(request, imageBytes)
            .map { it.toRequestResult() }
    }

    override fun updateFood(
        foodId: Long,
        request: FoodCreateRequestDTO,
        imageBytes: ByteArray?
    ): Flow<RequestResult> {
        return remoteDataSource
            .requestUpdateFood(foodId,request, imageBytes)
            .map { it.toRequestResult() }
    }

    override fun consumptionFood(
        foodId: Long
    ): Flow<RequestResult> {
        return remoteDataSource
            .requestConsumptionFood(foodId)
            .map { it.toRequestResult() }
    }

    override fun getFoodCount(): Flow<FoodCountDTO> {
        return remoteDataSource.getFoodCount()

    }
}