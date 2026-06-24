package xyz.malefic.kanman.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserRequestModel(
    val username: String,
    val password: String,
)

@Serializable
data class UserResponseModel(
    val id: Uuid,
    val username: String,
    val boards: List<BoardModel>,
)

@Serializable
data class RefreshRequestModel(
    @SerialName("refresh_token")
    val refreshToken: String,
) {
    companion object {
        val String.refresh get() = RefreshRequestModel(this)
    }
}

@Serializable
data class TokenResponseModel(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

@Serializable
data class StickyNoteModel(
    val id: Uuid,
    val title: String,
    val content: String,
    val column: Column,
    val board: BoardModel,
)

@Serializable
data class BoardCreateModel(
    val title: String,
    val visibility: Visibility,
)

@Serializable
data class BoardModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val owner: UserResponseModel,
    val stickies: List<StickyNoteModel>,
    val users: List<UserResponseModel>,
)

@Serializable
data class BoardSummaryModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val owner: UserResponseModel,
)

@Serializable
data class InviteRequest(
    @SerialName("user_id")
    val userId: Uuid,
) {
    companion object {
        val Uuid.invite get() = InviteRequest(this)
    }
}
