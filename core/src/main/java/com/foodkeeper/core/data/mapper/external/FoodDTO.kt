package com.foodkeeper.core.data.mapper.external

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodListDTO(
    @SerialName("content")
    val content: String,
    @SerialName("hasNext")
    val hasNext: Long?
)

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