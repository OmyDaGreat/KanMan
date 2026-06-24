package xyz.malefic.kanman.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface WsAction {
    @Serializable
    @SerialName("sticky_create")
    data class StickyCreate(
        val title: String,
        val content: String?,
        val column: Column,
    ) : WsAction

    @Serializable
    @SerialName("sticky_move")
    data class StickyMove(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("new_column") val newColumn: Column,
    ) : WsAction

    @Serializable
    @SerialName("sticky_delete")
    data class StickyDelete(
        @SerialName("sticky_id") val stickyId: Uuid,
    ) : WsAction

    @Serializable
    @SerialName("assign_user")
    data class AssignUser(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("user_id") val userId: Uuid,
    ) : WsAction

    @Serializable
    @SerialName("unassign_user")
    data class UnassignUser(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("user_id") val userId: Uuid,
    ) : WsAction
}

@Serializable
sealed interface WsEvent {
    @Serializable
    @SerialName("user_join")
    data class UserJoin(
        @SerialName("board_id") val boardId: Uuid,
        val user: UserSummaryModel,
    ) : WsEvent

    @Serializable
    @SerialName("user_leave")
    data class UserLeave(
        @SerialName("board_id") val boardId: Uuid,
        val user: UserSummaryModel,
    ) : WsEvent

    @Serializable
    @SerialName("sticky_created")
    data class StickyCreated(
        val sticky: StickyNoteModel,
        val creator: UserSummaryModel,
    ) : WsEvent

    @Serializable
    @SerialName("sticky_moved")
    data class StickyMoved(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("new_column") val newColumn: Column,
    ) : WsEvent

    @Serializable
    @SerialName("sticky_deleted")
    data class StickyDeleted(
        @SerialName("sticky_id") val stickyId: Uuid,
    ) : WsEvent

    @Serializable
    @SerialName("assigned_user")
    data class AssignedUser(
        @SerialName("sticky_id") val stickyId: Uuid,
        val user: UserSummaryModel,
    ) : WsEvent

    @Serializable
    @SerialName("unassigned_user")
    data class UnassignedUser(
        @SerialName("sticky_id") val stickyId: Uuid,
        val user: UserSummaryModel,
    ) : WsEvent
}
