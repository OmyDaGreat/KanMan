package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserResponseModel(
    val id: Uuid,
    val username: String,
    val email: String,
    val profilePicture: String,
    val boards: List<BoardSummaryModel>,
) {
    fun toSummaryModel() = UserSummaryModel(id, username, profilePicture)
}

@Serializable
data class UserSummaryModel(
    val id: Uuid,
    val username: String,
    val profilePicture: String,
)

@Serializable
data class UserUpdateModel(
    val username: String? = null,
    val profilePicture: String? = null,
)
