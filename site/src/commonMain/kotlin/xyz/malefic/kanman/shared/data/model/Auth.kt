package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRequestModel(
    val username: String,
    val password: String,
)

@Serializable
data class TokenModel(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
) {
    val response = TokenResponseModel(accessToken, expiresIn)
}

@Serializable
data class TokenResponseModel(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
)
