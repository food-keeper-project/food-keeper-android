package com.foodkeeper.core.data.mapper.External

import com.foodkeeper.core.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AutoTokenDTO(
    @SerialName("accesstoken")
    val accessToken: String,

    @SerialName("refreshToken")
    val refreshToken: String
)


fun AutoTokenDTO.toUser(): User {
    return User(
        id = 123,
        name = "123",
        email = "",
        password = ""
    )
}