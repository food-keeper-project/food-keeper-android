package com.foodkeeper.core.data.mapper.external

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class ProfileDTO(
    @SerialName("nickname")
    val nickname: String,
    @SerialName("imageUrl")
    val imageUrl: String?
)
