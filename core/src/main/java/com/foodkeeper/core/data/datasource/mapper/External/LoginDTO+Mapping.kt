package com.foodkeeper.core.data.datasource.mapper.External

import com.foodkeeper.core.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserDTO(
    @SerialName("name")
    val name: String,
)


fun UserDTO.toUser(): User {
    return User(
        id = 123,
        name = name,
        email = "",
        password = ""
    )
}