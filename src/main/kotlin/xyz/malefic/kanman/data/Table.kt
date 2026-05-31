package xyz.malefic.kanman.data

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

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
    val board = reference("board_id", Boards)
}

object BoardUsers : Table("board_users") {
    val board = reference("board_id", Boards)
    val user = reference("user_id", Users)
    override val primaryKey = PrimaryKey(board, user)
}

/**
 * Enum representing the columns in a Kanban board.
 */
enum class Column {
    BACKLOG,
    PLANNING,
    IN_PROGRESS,
    DONE,
}

/**
 * Enum representing the visibility of a Kanban board.
 */
enum class Visibility {
    PUBLIC,
    PRIVATE,
}
