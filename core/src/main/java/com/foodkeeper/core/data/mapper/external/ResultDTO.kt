package com.foodkeeper.core.data.mapper.external

import android.R.attr.name
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.RequestResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultDTO(
    @SerialName("result")
    val result: String
)



fun ResultDTO.toRequestResult(): RequestResult {
    return RequestResult(
        result = this.result == "SUCCESS"
    )
}