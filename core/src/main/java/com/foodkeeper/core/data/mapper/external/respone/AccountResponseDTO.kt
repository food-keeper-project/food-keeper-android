package com.foodkeeper.core.data.mapper.external.respone

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountResponseDTO(
    @SerialName("isDuplicated")
    val isDuplicated: Boolean
)
