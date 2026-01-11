package com.foodkeeper.core.data.mapper.request

import kotlinx.serialization.Serializable

@Serializable
data class AccountRequestDTO(
    val account:String
)



@Serializable
data class SignUpRequestDTO(
    val account: String = "",
    val password: String = "",
    val email: String = "",
    val nickname: String = "",
    val gender: String = "N", // "M", "F"
)

