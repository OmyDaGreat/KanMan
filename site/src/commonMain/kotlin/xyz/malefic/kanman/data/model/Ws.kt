package xyz.malefic.kanman.data.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

sealed class WsEvent {
    @Serializable
    data class UserJoin(
        val boardId: Uuid,
        val username: UserResponseModel,
    ) : WsEvent()

    @Serializable
    data class UserLeave(
        val boardId: Uuid,
        val username: UserResponseModel,
    ) : WsEvent()

    @Serializable
    data class StickyCreate(
        val boardId: Uuid,
        val creator: UserResponseModel,
        val sticky: StickyNoteModel,
    ) : WsEvent()
}

@Serializable
data class StickyCreateModel(
    val title: String,
    val content: String?,
    val column: Column,
)
