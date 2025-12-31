package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.CategoryDTO
import com.foodkeeper.core.data.mapper.external.FoodDTO
import com.foodkeeper.core.data.mapper.external.FoodListDTO
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.data.mapper.external.ResultDTO
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
    fun requestFoodList(limit: Int, categoryId: Long?, cursor: Long?): Flow<FoodListDTO> {
        return apiService.request(
            ApiRoute.AllFoodList(
                limit = limit,
                categoryId = categoryId,
                cursor = cursor
            )
        )
    }
    // 유통기한 임박 식재료 조회
    fun requestImminentFoodList(): Flow<List<FoodDTO>> {
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

    //전체 카테고리 리스트 조회 (추후 카테고리 파일로 분리 예정 바쁘다 ㅜㅠㅠ)
    fun requestCategorieList(): Flow<List<CategoryDTO>> {
        return apiService.request(
            ApiRoute.Categories
        )
    }

}
