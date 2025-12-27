package com.foodkeeper.core.data.mapper.external

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToKenDTO(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("refreshToken")
    val refreshToken: String
)