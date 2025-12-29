package com.foodkeeper.core.data.mapper.external

import com.foodkeeper.core.domain.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String
)

fun CategoryDTO.toCategory(): Category {
    return Category(
        id = id,
        name = name
    )
}