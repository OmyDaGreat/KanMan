package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface WsAction {
    @Serializable
    data class StickyCreate(
        val title: String,
        val content: String?,
        val column: Column,
    ) : WsAction

    @Serializable
    data class StickyMove(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("new_column") val newColumn: Column,
    ) : WsAction

    @Serializable
    data class StickyDelete(
        @SerialName("sticky_id") val stickyId: Uuid,
    ) : WsAction

    @Serializable
    data class AssignUser(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("user_id") val userId: Uuid,
        val due: Instant? = null,
    ) : WsAction

    @Serializable
    data class UnassignUser(
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("user_id") val userId: Uuid,
    ) : WsAction
}

@Serializable
sealed interface WsEvent {
    val actor: UserSummaryModel

    @Serializable
    data class UserJoin(
        override val actor: UserSummaryModel,
        @SerialName("board_id") val boardId: Uuid,
    ) : WsEvent

    @Serializable
    data class UserLeave(
        override val actor: UserSummaryModel,
        @SerialName("board_id") val boardId: Uuid,
    ) : WsEvent

    @Serializable
    data class StickyCreated(
        override val actor: UserSummaryModel,
        val sticky: StickyNoteModel,
    ) : WsEvent

    @Serializable
    data class StickyMoved(
        override val actor: UserSummaryModel,
        @SerialName("sticky_id") val stickyId: Uuid,
        @SerialName("new_column") val newColumn: Column,
    ) : WsEvent

    @Serializable
    data class StickyDeleted(
        override val actor: UserSummaryModel,
        @SerialName("sticky_id") val stickyId: Uuid,
    ) : WsEvent

    @Serializable
    data class AssignedUser(
        override val actor: UserSummaryModel,
        @SerialName("sticky_id") val stickyId: Uuid,
        val target: UserSummaryModel,
        val due: Instant? = null,
    ) : WsEvent

    @Serializable
    data class UnassignedUser(
        override val actor: UserSummaryModel,
        @SerialName("sticky_id") val stickyId: Uuid,
        val target: UserSummaryModel,
    ) : WsEvent
}
