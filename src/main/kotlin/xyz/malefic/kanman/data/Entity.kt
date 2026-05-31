@file:OptIn(ExperimentalUuidApi::class)

package xyz.malefic.kanman.data

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<UserEntity>(Users)

    var username by Users.username
    var password by Users.password
    var boards by BoardEntity via BoardUsers
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
