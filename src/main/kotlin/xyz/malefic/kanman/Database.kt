@file:OptIn(ExperimentalUuidApi::class)

package xyz.malefic.kanman

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.ExperimentalUuidApi

object Users : UuidTable("users") {
    val username = varchar("username", 128).uniqueIndex()
    val password = varchar("password", 256)
}

object Boards : UuidTable("boards") {
    val title = varchar("title", 128)
    val visibility = enumeration<Visibility>("visibility")
}

object StickyNotes : UuidTable("stickies") {
    val title = varchar("title", 256)
    val content = text("content")
    val column = enumeration<Column>("column")
    val boardId = uuid("board_id")
}

object BoardUsers : Table("board_users") {
    val boardId = uuid("board_id")
    val userId = uuid("user_id")
}
