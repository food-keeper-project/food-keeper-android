package com.foodkeeper.core.data.mapper.external

import android.R
import android.provider.Settings.System.DATE_FORMAT
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.StorageMethod
import com.foodkeeper.core.domain.model.User
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
    @SerialName("categoryIds")
    val categoryIds: List<CategoryDTO>
)




fun FoodDTO.toFood(): Food {
    return Food(
        id = id,
        name = name,
        imageURL = imageUrl,
        storageMethod = toStorageMethod(storageMethod),
        expiryDate = expiryDate.parseServerDate(),
        memo = memo,
        createdAt = createdAt.parseServerDate(),
        category = categoryIds.first().name,
        categoryModel = categoryIds.map { it.toCategory() },
        expiryAlarm = 0 //추후 응답주면 변경 필요
    )
}

private fun toStorageMethod(storageMethod: String): StorageMethod =
    when (storageMethod) {
        "REFRIGERATED" -> StorageMethod.REFRIGERATED
        "FROZEN" -> StorageMethod.FREEZER
        "ROOM_TEMP" -> StorageMethod.ROOM_TEMP
        else -> StorageMethod.REFRIGERATED // 기본값 (원하는 값으로)
    }