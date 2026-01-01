package com.foodkeeper.core.domain.model

import android.net.Uri
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import java.util.Date

data class AddFoodInput(
    val name: String = "", //식품명
    val imageUri: Uri? = null, //이미지 URL
    val categorys: List<Category> = emptyList(), //카테고리
    val storageMethod: StorageMethod? = null, // 보관방식
    val expiryDate: Date? = null, //유통기한 날짜
    val expiryAlarm: ExpiryAlarm? = null, // 알람일
    val memo: String = "", // 메모
)


fun AddFoodInput.toRequest(): FoodCreateRequestDTO {
    return FoodCreateRequestDTO(
        name = name,
        categoryIds = categorys.map { it.id },
        storageMethod = storageMethod?.name ?: StorageMethod.REFRIGERATED.name,
        expiryDate = expiryDate?.toString() ?: "",
        expiryAlarm = expiryAlarm?.daysBefore ?: ExpiryAlarm.THREE_DAYS.daysBefore,
        memo = memo
    )
}