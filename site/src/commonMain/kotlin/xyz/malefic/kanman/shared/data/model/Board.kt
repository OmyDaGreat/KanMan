package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class Visibility {
    PUBLIC,
    PRIVATE,
}

@Serializable
enum class BoardAction {
    DELETE_BOARD,
    INVITE_USER,
    EDIT_STICKY,
    VIEW_BOARD,
}

@Serializable
enum class Role(
    val permission: (BoardAction) -> Boolean,
) {
    OWNER({ true }),
    ADMIN({
        when (it) {
            BoardAction.DELETE_BOARD -> false
            BoardAction.INVITE_USER -> true
            BoardAction.EDIT_STICKY -> true
            BoardAction.VIEW_BOARD -> true
        }
    }),
    MEMBER({
        when (it) {
            BoardAction.DELETE_BOARD -> false
            BoardAction.INVITE_USER -> false
            BoardAction.EDIT_STICKY -> true
            BoardAction.VIEW_BOARD -> true
        }
    }),
    GUEST({
        when (it) {
            BoardAction.VIEW_BOARD -> true
            else -> false
        }
    }),
}

@Serializable
data class BoardUserResponseModel(
    val user: UserSummaryModel,
    val role: Role,
    val lastViewedAt: Instant,
)

@Serializable
data class BoardCreateModel(
    val title: String,
    val description: String = "",
    val visibility: Visibility,
)

@Serializable
data class BoardResponseModel(
    val id: Uuid,
    val title: String,
    val description: String = "",
    val visibility: Visibility,
    val owner: UserSummaryModel,
    val stickies: List<StickyNoteModel>,
    val memberships: List<BoardUserResponseModel>,
)

@Serializable
data class BoardSummaryModel(
    val id: Uuid,
    val title: String,
    val description: String = "",
    val visibility: Visibility,
    val owner: UserSummaryModel,
    val lastViewedAt: Instant? = null,
)

@Serializable
data class BoardEventModel(
    val id: Uuid,
    @SerialName("board_id") val boardId: Uuid,
    val actor: UserSummaryModel,
    val event: WsEvent,
    val timestamp: Instant = Clock.System.now(),
)
