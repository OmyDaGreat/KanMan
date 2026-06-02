package xyz.malefic.kanman.data

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class UserEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<UserEntity>(Users)

    var username by Users.username
    var hashedPassword by Users.hashedPassword
    var boards by BoardEntity via BoardUsers
}

class AuthTokenEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<AuthTokenEntity>(AuthTokens)

    var user by UserEntity referencedOn AuthTokens.user
    var tokenHash by AuthTokens.tokenHash
    var tokenType by AuthTokens.tokenType
    var expiresAt by AuthTokens.expiresAt
    var revokedAt by AuthTokens.revokedAt
}

class BoardEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<BoardEntity>(Boards)

    var title by Boards.title
    var visibility by Boards.visibility
    val stickies by StickyNoteEntity referrersOn StickyNotes.board
    var users by UserEntity via BoardUsers
}

class StickyNoteEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<StickyNoteEntity>(StickyNotes)

    var title by StickyNotes.title
    var content by StickyNotes.content
    var column by StickyNotes.column
    var board by BoardEntity referencedOn StickyNotes.board
}
