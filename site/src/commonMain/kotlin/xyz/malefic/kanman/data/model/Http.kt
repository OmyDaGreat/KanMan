package xyz.malefic.kanman.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
@SerialName("paginated_response")
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
)

@Serializable
@SerialName("user_request")
data class UserRequestModel(
    val username: String,
    val password: String,
)

@Serializable
@SerialName("user")
data class UserResponseModel(
    val id: Uuid,
    val username: String,
    val boards: List<BoardSummaryModel>,
) {
    fun toSummaryModel() = UserSummaryModel(id, username)
}

@Serializable
@SerialName("user_summary")
data class UserSummaryModel(
    val id: Uuid,
    val username: String,
)

@Serializable
@SerialName("refresh_request")
data class RefreshRequestModel(
    @SerialName("refresh_token") val refreshToken: String,
) {
    companion object {
        val String.refresh get() = RefreshRequestModel(this)
    }
}

@Serializable
@SerialName("token_response")
data class TokenResponseModel(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
)

@Serializable
@SerialName("sticky_note")
data class StickyNoteModel(
    val id: Uuid,
    val title: String,
    val content: String,
    val column: Column,
    @SerialName("assigned_users") val assignedUsers: List<Uuid>,
    @SerialName("board_id") val boardId: Uuid,
)

@Serializable
@SerialName("board_create")
data class BoardCreateModel(
    val title: String,
    val visibility: Visibility,
)

@Serializable
@SerialName("board")
data class BoardResponseModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val owner: UserSummaryModel,
    val stickies: List<StickyNoteModel>,
    val users: List<UserSummaryModel>,
)

@Serializable
@SerialName("board_summary")
data class BoardSummaryModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val owner: UserSummaryModel,
)

@Serializable
@SerialName("board_event")
data class BoardEventModel(
    val id: Uuid,
    @SerialName("board_id") val boardId: Uuid,
    val actor: UserSummaryModel,
    val event: WsEvent,
    val timestamp: Instant = Clock.System.now(),
)

@Serializable
@SerialName("invite_request")
data class InviteRequest(
    @SerialName("user_id") val userId: Uuid,
) {
    companion object {
        val Uuid.invite get() = InviteRequest(this)
    }
}
