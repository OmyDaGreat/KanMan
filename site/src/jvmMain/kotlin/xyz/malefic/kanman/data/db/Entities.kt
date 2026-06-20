package xyz.malefic.kanman.data.db

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import xyz.malefic.kanman.data.model.BoardModel
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.StickyNoteModel
import xyz.malefic.kanman.data.model.UserResponseModel
import kotlin.uuid.Uuid

class UserEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<UserEntity>(Users)

    var username by Users.username
    var hashedPassword by Users.hashedPassword
    var boards by BoardEntity via BoardUsers

    fun toResponseModel(): UserResponseModel = UserResponseModel(id.value, username, boards.map { it.toModel() })
}

class AuthTokenEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<AuthTokenEntity>(AuthTokens)

    var user by UserEntity referencedOn AuthTokens.user
    var secretHash by AuthTokens.secretHash
    var expiresAt by AuthTokens.expiresAt
    var revokedAt by AuthTokens.revokedAt
}

class BoardEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<BoardEntity>(Boards)

    var title by Boards.title
    var visibility by Boards.visibility
    var owner by UserEntity referencedOn Boards.owner
    val stickies by StickyNoteEntity referrersOn StickyNotes.board
    var users by UserEntity via BoardUsers

    fun toModel() =
        BoardModel(
            id.value,
            title,
            visibility,
            owner.toResponseModel(),
            stickies.map { it.toModel() },
            users.map { it.toResponseModel() },
        )

    fun toSummaryModel() = BoardSummaryModel(id.value, title, visibility, owner.toResponseModel())
}

class StickyNoteEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<StickyNoteEntity>(StickyNotes)

    var title by StickyNotes.title
    var content by StickyNotes.content
    var column by StickyNotes.column
    var board by BoardEntity referencedOn StickyNotes.board

    fun toModel(): StickyNoteModel = StickyNoteModel(id.value, title, content, column, board.toModel())
}
