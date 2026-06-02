package xyz.malefic.kanman.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.malefic.kanman.util.lens
import xyz.malefic.kanman.util.wsLens
import kotlin.uuid.Uuid

@Serializable
data class ErrorModel(
    val error: String,
)

val String.errorModel: ErrorModel
    get() = ErrorModel(this)

val errorLens = lens<ErrorModel>()

@Serializable
data class UserRequestModel(
    val username: String,
    val password: String,
)

@Serializable
data class UserResponseModel(
    val id: Uuid,
    val username: String,
    val boards: List<BoardModel>,
)

fun UserEntity.toResponseModel(): UserResponseModel = UserResponseModel(id.value, username, boards.map { it.toModel() })

val userResponseLens = lens<UserResponseModel>()

@Serializable
data class RefreshRequestModel(
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
data class TokenResponseModel(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

val tokenResponseLens = lens<TokenResponseModel>()

@Serializable
data class StickyNoteModel(
    val id: Uuid,
    val title: String,
    val content: String,
    val column: Column,
    val board: BoardModel,
)

fun StickyNoteEntity.toModel(): StickyNoteModel = StickyNoteModel(id.value, title, content, column, board.toModel())

@Serializable
data class StickyCreateModel(
    val title: String,
    val content: String,
    val column: Column,
)

val stickyCreateLens = wsLens<StickyCreateModel>()

@Serializable
data class BoardCreateModel(
    val title: String,
    val visibility: Visibility,
)

@Serializable
data class BoardModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val owner: UserResponseModel,
    val stickies: List<StickyNoteModel>,
    val users: List<UserResponseModel>,
)

fun BoardEntity.toModel() =
    BoardModel(
        id.value,
        title,
        visibility,
        owner.toResponseModel(),
        stickies.map {
            it.toModel()
        },
        users.map { it.toResponseModel() },
    )

val boardLens = lens<BoardModel>()

@Serializable
data class BoardSummaryModel(
    val id: Uuid,
    val title: String,
    val visibility: Visibility,
    val owner: UserResponseModel,
)

fun BoardEntity.toSummaryModel() = BoardSummaryModel(id.value, title, visibility, owner.toResponseModel())

val boardSummaryListLens = lens<List<BoardSummaryModel>>()
