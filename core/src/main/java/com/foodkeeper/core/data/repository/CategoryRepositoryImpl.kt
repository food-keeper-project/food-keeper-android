package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.datasource.external.CategoryRemoteDataSource
import com.foodkeeper.core.data.datasource.external.FoodRemoteDataSource
import com.foodkeeper.core.data.mapper.external.toCategory
import com.foodkeeper.core.data.mapper.external.toFood
import com.foodkeeper.core.data.mapper.external.toRequestResult
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.RequestResult
import com.foodkeeper.core.domain.repository.CategoryRepository
import com.foodkeeper.core.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSource
) : CategoryRepository {

    override fun getCategorieList(): Flow<List<Category>> {
        return remoteDataSource
            .requestCategorieList()
            .map { dto ->
                dto.map { it.toCategory()}
            }
    }
}