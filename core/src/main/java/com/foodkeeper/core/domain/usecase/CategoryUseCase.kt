package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.repository.CategoryRepository
import com.foodkeeper.core.domain.repository.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface CategoryUseCase {
    //카테고리 리스트 조회
    fun getFoodCategoryList(): Flow<List<String>>
}

class DefaultCategoryUseCase @Inject constructor(
    private val CategoryRepository: CategoryRepository
) : CategoryUseCase {

    //카테고리 리스트 조회
    override fun getFoodCategoryList(): Flow<List<String>> {
        val categorys = CategoryRepository.getCategorieList().map {
            it.map { it.name }
        }
        return categorys
    }

}