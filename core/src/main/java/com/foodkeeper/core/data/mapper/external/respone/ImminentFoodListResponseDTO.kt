package com.foodkeeper.core.data.mapper.external.respone

import com.foodkeeper.core.data.mapper.external.FoodDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImminentFoodListResponseDTO(
    @SerialName("foods")
    val content: List<FoodDTO>,
)
