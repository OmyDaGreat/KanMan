package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserResponseModel(
    val id: Uuid,
    val username: String,
    val boards: List<BoardSummaryModel>,
) {
    fun toSummaryModel() = UserSummaryModel(id, username)
}

@Serializable
data class UserSummaryModel(
    val id: Uuid,
    val username: String,
)
