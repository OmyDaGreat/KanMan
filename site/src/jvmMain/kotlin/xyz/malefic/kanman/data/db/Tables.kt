package xyz.malefic.kanman.data.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import xyz.malefic.kanman.data.model.Column
import xyz.malefic.kanman.data.model.Visibility

object Users : UuidTable("users") {
    val username = varchar("username", 128).uniqueIndex()
    val hashedPassword = varchar("password", 256)
}

object AuthTokens : UuidTable("auth_tokens") {
    val user = reference("user_id", Users)
    val secretHash = varchar("secret_hash", 64)
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
