package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.CategoryDTO
import com.foodkeeper.core.data.mapper.external.FoodDTO
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.data.mapper.external.ResultDTO
import com.foodkeeper.core.data.mapper.external.respone.AllFoodListResponseDTO
import com.foodkeeper.core.data.mapper.external.respone.ImminentFoodListResponseDTO
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 하나로 관리할 때는 Singleton 어노테이션을 붙여주는 것이 좋습니다.
class FoodRemoteDataSource @Inject constructor(
    private val apiService: FoodApiService
) {
    // 전체 식재료 조회
    fun requestFoodList(limit: Int, categoryId: Long?, cursor: Long?): Flow<AllFoodListResponseDTO> {
        return apiService.request(
            ApiRoute.AllFoodList(
                limit = limit,
                categoryId = categoryId,
                cursor = cursor
            )
        )
    }
    // 유통기한 임박 식재료 조회
    fun requestImminentFoodList(): Flow<ImminentFoodListResponseDTO> {
        return apiService.request(
            ApiRoute.ImminentFoodList
        )
    }
    //식재료 추가
    fun requestAddFood(request: FoodCreateRequestDTO, imageBytes: ByteArray?): Flow<ResultDTO> {
        return apiService.request(
            ApiRoute.AddFood(
                request = request,
                imageBytes = imageBytes
            )
        )
    }

    fun requestConsumptionFood(foodId: Long): Flow<ResultDTO> {
        return apiService.request(
            ApiRoute.ConsumptionFood(
                foodId = foodId
            )
        )
    }

}
