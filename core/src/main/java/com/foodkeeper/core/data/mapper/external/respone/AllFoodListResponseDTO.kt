package com.foodkeeper.core.data.mapper.external.respone

import com.foodkeeper.core.data.mapper.external.FoodDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllFoodListResponseDTO(
    @SerialName("content")
    val content: List<FoodDTO>,
    @SerialName("hasNext")
    val hasNext: Boolean
)
