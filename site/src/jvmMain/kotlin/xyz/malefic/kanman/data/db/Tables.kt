package xyz.malefic.kanman.data.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import xyz.malefic.kanman.api.util.json
import xyz.malefic.kanman.data.model.Column
import xyz.malefic.kanman.data.model.Role
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.WsEvent

object Users : UuidTable("users") {
    val username = varchar("username", 128).uniqueIndex()
    val hashedPassword = varchar("password", 256)
    val failedAttempts = integer("failed_attempts").default(0)
    val lockUntil = long("lock_until").default(0)
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

object BoardEvents : UuidTable("board_events") {
    val board = reference("board_id", Boards, onDelete = ReferenceOption.CASCADE)
    val actor = reference("actor_id", Users, onDelete = ReferenceOption.CASCADE)
    val event = jsonb<WsEvent>("event", json)
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}

object StickyNotes : UuidTable("stickies") {
    val title = varchar("title", 256)
    val content = text("content")
    val column = enumeration<Column>("column")
    val board = reference("board_id", Boards)
}

object AssignedUsers : CompositeIdTable("assigned_stickies") {
    val sticky = reference("sticky_id", StickyNotes, onDelete = ReferenceOption.CASCADE)
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val due = timestamp("due").nullable()

    init {
        addIdColumn(sticky)
        addIdColumn(user)
    }

    override val primaryKey = PrimaryKey(sticky, user)
}

object BoardUsers : CompositeIdTable("board_users") {
    val board = reference("board_id", Boards, onDelete = ReferenceOption.CASCADE)
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val role = enumeration<Role>("role")
    val lastViewedAt = timestamp("last_viewed_at").defaultExpression(CurrentTimestamp)

    init {
        addIdColumn(board)
        addIdColumn(user)
    }

    override val primaryKey = PrimaryKey(board, user)
}
