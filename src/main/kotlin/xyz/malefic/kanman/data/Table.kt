package xyz.malefic.kanman.data

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object Users : UuidTable("users") {
    val username = varchar("username", 128).uniqueIndex()
    val hashedPassword = varchar("password", 256)
}

object AuthTokens : UuidTable("auth_tokens") {
    val user = reference("user_id", Users)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val tokenType = enumeration<TokenType>("kind")
    val expiresAt = long("expires_at")
    val revokedAt = long("revoked_at").nullable()
}

object Boards : UuidTable("boards") {
    val title = varchar("title", 128)
    val visibility = enumeration<Visibility>("visibility")
    val owner = reference("owner_id", Users)
}

object StickyNotes : UuidTable("stickies") {
    val title = varchar("title", 256)
    val content = text("content")
    val column = enumeration<Column>("column")
    val board = reference("board_id", Boards)
}

object BoardUsers : Table("board_users") {
    val board = reference("board_id", Boards, onDelete = ReferenceOption.CASCADE)
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
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

enum class TokenType {
    ACCESS,
    REFRESH,
}
