package com.foodkeeper.core.data.mapper.external

import com.foodkeeper.core.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiError(
    val errorCode: String,
    val message: String,
    // ✅ 서버에서 data 내부에 어떤 객체(cause, stackTrace 등)를 보낼지 모르므로
    // JsonElement로 받아야 파싱 에러(Unexpected JSON token)를 막을 수 있습니다.
    val data: JsonElement? = null
)
@Serializable
data class AuthTokenDTO(
    @SerialName("accessToken")
    val accessToken: String?,
    @SerialName("refreshToken")
    val refreshToken: String?
)
@Serializable
data class ApiResponse<T>(
    val result: String?=null,
    val data: T? = null,
    val error: ApiError? = null
)

fun AuthTokenDTO.toUser(): User {
    return User(
        id = 123,
        name = "123",
        email = "",
        password = ""
    )
}