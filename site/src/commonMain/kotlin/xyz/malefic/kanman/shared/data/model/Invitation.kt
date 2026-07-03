package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class InviteRequest(
    @SerialName("board_id") val boardId: Uuid,
    @SerialName("user_id") val userId: Uuid,
    val role: Role,
)

@Serializable
data class Invitation(
    val id: Uuid,
    @SerialName("board_id") val boardId: Uuid,
    @SerialName("sender_id") val senderId: Uuid,
    @SerialName("receiver_id") val receiverId: Uuid,
    val role: Role,
)

@Serializable
data class RoleUpdateRequest(
    val role: Role,
)
