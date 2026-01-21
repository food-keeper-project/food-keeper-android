package com.foodkeeper.core.data.mapper.external

import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.StorageMethod

import com.foodkeeper.core.ui.util.parseServerDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FoodDTO(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("imageUrl")
    val imageUrl: String,
    @SerialName("storageMethod")
    val storageMethod: String,
    @SerialName("expiryDate")
    val expiryDate: String,      // "2025-12-29"
    @SerialName("memo")
    val memo: String,
    @SerialName("createdAt")
    val createdAt: String,       // "2025-12-29T14:33:52.752Z"
    @SerialName("categories")
    val categoryIds: List<CategoryDTO>,
    @SerialName("expiryAlarm")
    val expiryAlarm: Int
)


@Serializable
data class FoodCountDTO(
    val foodCount:Long?
)

fun FoodDTO.toFood(): Food {
    // 제공해주신 NCloud Storage Base URL
    val IMAGE_BASE_URL = "https://kitchenlog.gcdn.ntruss.com/"

    // 서버가 준 imageUrl 앞에 baseurl을 결합
    val fullImageUrl = if (!imageUrl.isNullOrEmpty()) {
        "${IMAGE_BASE_URL}${imageUrl}"
    } else {
        null
    }

    return Food(
        id = id,
        name = name,
        imageURL = fullImageUrl?:"", // 이제 UI에서는 이 주소를 바로 사용하면 됩니다.
        storageMethod = toStorageMethod(storageMethod),
        expiryDate = expiryDate.parseServerDate(),
        memo = memo ?: "",
        createdAt = createdAt.parseServerDate(),
        category = categoryIds.firstOrNull()?.name ?: "미분류",
        categoryModel = categoryIds.map { it.toCategory() },
        expiryAlarm = expiryAlarm
    )
}

private fun toStorageMethod(storageMethod: String): StorageMethod =
    when (storageMethod) {
        "REFRIGERATED" -> StorageMethod.REFRIGERATED
        "FROZEN" -> StorageMethod.FREEZER
        "ROOM_TEMP" -> StorageMethod.ROOM_TEMP
        else -> StorageMethod.REFRIGERATED // 기본값 (원하는 값으로)
    }