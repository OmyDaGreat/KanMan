package xyz.malefic.kanman.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

sealed class WsEvent {
    @Serializable
    @SerialName("user_join")
    data class UserJoin(
        @SerialName("board_id")
        val boardId: Uuid,
        val username: UserResponseModel,
    ) : WsEvent()

    @Serializable
    @SerialName("user_leave")
    data class UserLeave(
        @SerialName("board_id")
        val boardId: Uuid,
        val username: UserResponseModel,
    ) : WsEvent()

    @Serializable
    @SerialName("sticky_create")
    data class StickyCreate(
        @SerialName("board_id")
        val title: String,
        val content: String?,
        val column: Column,
    ) : WsEvent()

    @Serializable
    @SerialName("sticky_update")
    data class StickyMove(
        @SerialName("sticky_id")
        val stickyId: Uuid,
        val newColumn: Column,
    ) : WsEvent()

    @Serializable
    @SerialName("sticky_delete")
    data class StickyDelete(
        @SerialName("sticky_id")
        val stickyId: Uuid,
    ) : WsEvent()
}
