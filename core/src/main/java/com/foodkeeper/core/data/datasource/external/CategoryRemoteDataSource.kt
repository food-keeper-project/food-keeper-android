package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.CategoryDTO
import com.foodkeeper.core.data.mapper.external.FoodDTO
import com.foodkeeper.core.data.mapper.external.ResultDTO
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 하나로 관리할 때는 Singleton 어노테이션을 붙여주는 것이 좋습니다.
class CategoryRemoteDataSource @Inject constructor(
    private val apiService: FoodApiService
) {
    //전체 카테고리 리스트 조회 (추후 카테고리 파일로 분리 예정 바쁘다 ㅜㅠㅠ)
    fun requestCategorieList(): Flow<List<CategoryDTO>> {
        return apiService.request(
            ApiRoute.Categories
        )
    }

}
