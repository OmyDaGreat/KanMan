package xyz.malefic.kanman.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import kotlin.uuid.Uuid

val idLens = Body.auto<Uuid>().toLens()

@Serializable
data class ErrorModel(
    val error: String,
)

val String.error: ErrorModel
    get() = ErrorModel(this)

val errorLens = Body.auto<ErrorModel>().toLens()

@Serializable
data class UserRequestModel(
    val username: String,
    val password: String,
)

val userRequestLens = Body.auto<UserRequestModel>().toLens()

@Serializable
data class UserResponseModel(
    val id: Uuid,
    val username: String,
    val boards: List<BoardModel>,
)

fun UserEntity.toResponseModel(): UserResponseModel = UserResponseModel(id.value, username, boards.map { it.toModel() })

val userResponseLens = Body.auto<UserResponseModel>().toLens()

@Serializable
data class RefreshRequestModel(
    @SerialName("refresh_token")
    val refreshToken: String,
)

val refreshRequestLens = Body.auto<RefreshRequestModel>().toLens()

@Serializable
data class TokenResponseModel(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

val tokenResponseLens = Body.auto<TokenResponseModel>().toLens()

@Serializable
data class StickyNoteModel(
    val title: String,
    val content: String,
    val column: Column,
    val board: BoardModel,
)

fun StickyNoteEntity.toModel(): StickyNoteModel = StickyNoteModel(title, content, column, board.toModel())

@Serializable
data class BoardModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val stickies: List<StickyNoteModel>,
    val users: List<UserResponseModel>,
)

fun BoardEntity.toModel() = BoardModel(id.value, title, visibility, stickies.map { it.toModel() }, users.map { it.toResponseModel() })

val boardLens = Body.auto<BoardModel>().toLens()

@Serializable
data class BoardRequestModel(
    val title: String,
    val visibility: Visibility,
)

val boardRequestLens = Body.auto<BoardRequestModel>().toLens()

@Serializable
data class BoardListModel(
    val username: String,
    val boards: List<BoardModel>,
)

val boardListLens = Body.auto<BoardListModel>().toLens()
