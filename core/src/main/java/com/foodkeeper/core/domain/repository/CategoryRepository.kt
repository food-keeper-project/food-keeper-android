package com.foodkeeper.core.domain.repository

import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.RequestResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    /** 전체 카테고리 리스트 조회 */
    fun getCategorieList(): Flow<List<Category>>

}