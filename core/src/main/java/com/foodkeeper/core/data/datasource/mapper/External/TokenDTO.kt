package com.foodkeeper.core.data.datasource.mapper.External

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToKenDTO(
    @SerialName("accesstoken")
    val accessToken: String,

    @SerialName("refreshToken")
    val refreshToken: String
)