package com.foodkeeper.core.domain.usecase

import android.content.Context
import android.net.Uri
import com.foodkeeper.core.data.repository.UserRepository
import com.foodkeeper.core.domain.model.AddFoodInput
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.toRequest
import com.foodkeeper.core.domain.repository.FoodRepository
import com.foodkeeper.core.ui.util.isExpiringSoon
import dagger.hilt.android.qualifiers.ApplicationContext
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
    //식품 소비 요청
    fun ConsumptionFood(food: Food): Flow<Boolean>

    fun addFood(food: AddFoodInput): Flow<Boolean>
}

class DefaultFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
    @ApplicationContext private val context: Context
) : FoodUseCase {
    //전체 식자재 리스트 조회
    override fun getFoodList(): Flow<List<Food>> {
        return foodRepository.getFoodList(50, null, null)
    }
    //유통기한 임박 식자재 리스트 조회
    override fun getExpiringSoonFoodList(): Flow<List<Food>> {
        return foodRepository.getImminentFoodList()

    }
    //식재료 소비처리
    override fun ConsumptionFood(food: Food): Flow<Boolean> {
        return flow {
            delay(500)
            emit(true)
        }
    }

    //식재료 추가
    override fun addFood(food: AddFoodInput): Flow<Boolean> {
        val imageBytes = food.imageUri?.toByteArray(context)
        return foodRepository.addFood(food.toRequest(), imageBytes).map { requestResult ->
            requestResult.result
        }
    }
}
fun Uri.toByteArray(context: Context): ByteArray {
    return context.contentResolver.openInputStream(this)?.use { input ->
        input.readBytes()
    } ?: throw IllegalArgumentException("이미지 변환 실패")
}