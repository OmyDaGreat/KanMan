package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class Column {
    BACKLOG,
    PLANNING,
    IN_PROGRESS,
    DONE,
}

@Serializable
data class StickyNoteModel(
    val id: Uuid,
    val title: String,
    val content: String,
    val column: Column,
    @SerialName("assigned_users") val assignedUsers: List<AssignedUserModel>,
    @SerialName("board_id") val boardId: Uuid,
)

@Serializable
data class AssignedUserModel(
    @SerialName("user_id") val userId: Uuid,
    val due: Instant? = null,
)
