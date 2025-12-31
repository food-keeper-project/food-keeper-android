package com.foodkeeper.core.data.mapper.request

import kotlinx.serialization.Serializable

@Serializable
data class FoodCreateRequestDTO(
    val name: String,
    val categoryIds: List<Long>,
    val storageMethod: String,
    val expiryDate: String,
    val expiryAlarm: Int,
    val memo: String
)