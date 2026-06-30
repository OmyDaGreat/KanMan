package xyz.malefic.kanman.server.data.db

import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import xyz.malefic.kanman.shared.data.model.AssignedUserModel
import xyz.malefic.kanman.shared.data.model.BoardEventModel
import xyz.malefic.kanman.shared.data.model.BoardResponseModel
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel
import xyz.malefic.kanman.shared.data.model.BoardUserResponseModel
import xyz.malefic.kanman.shared.data.model.Invitation
import xyz.malefic.kanman.shared.data.model.Role
import xyz.malefic.kanman.shared.data.model.StickyNoteModel
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.UserSummaryModel
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<UserEntity>(Users)

    var username by Users.username
    var hashedPassword by Users.hashedPassword
    var failedAttempts by Users.failedAttempts
    var lockUntil by Users.lockUntil
    var boards by BoardEntity via BoardUsers

    fun toSummaryModel() = UserSummaryModel(id.value, username)

    fun toResponseModel() = UserResponseModel(id.value, username, boards.map { it.toSummaryModel(id.value) })
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
    val memberships by BoardUserEntity referrersOn BoardUsers.board
    val history by BoardEventEntity referrersOn BoardEvents.board

    fun toResponseModel() =
        BoardResponseModel(
            id.value,
            title,
            visibility,
            owner.toSummaryModel(),
            stickies.map { it.toModel() },
            memberships.map { it.toResponseModel() },
        )

    fun toSummaryModel(userId: Uuid? = null) =
        BoardSummaryModel(
            id.value,
            title,
            visibility,
            owner.toSummaryModel(),
            userId?.let {
                memberships
                    .find {
                        it.user.id.value ==
                            userId
                    }?.lastViewedAt
            },
        )
}

class BoardEventEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<BoardEventEntity>(BoardEvents)

    var board by BoardEntity referencedOn BoardEvents.board
    var actor by UserEntity referencedOn BoardEvents.actor
    var event by BoardEvents.event
    var timestamp by BoardEvents.timestamp

    fun toModel() = BoardEventModel(id.value, board.id.value, actor.toSummaryModel(), event, timestamp)
}

class StickyNoteEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<StickyNoteEntity>(StickyNotes)

    var title by StickyNotes.title
    var content by StickyNotes.content
    var column by StickyNotes.column
    val assignedUsers by AssignedUserEntity referrersOn AssignedUsers.sticky
    var board by BoardEntity referencedOn StickyNotes.board

    fun toModel() = StickyNoteModel(id.value, title, content, column, assignedUsers.map { it.toModel() }, board.id.value)
}

fun <A : CompositeEntityClass<B>, B : CompositeEntity> A.findById(id: (CompositeID) -> Unit) = findById(CompositeID(id))

class AssignedUserEntity(
    id: EntityID<CompositeID>,
) : CompositeEntity(id) {
    companion object : CompositeEntityClass<AssignedUserEntity>(AssignedUsers) {
        fun findById(
            stickyId: Uuid,
            userId: Uuid,
        ) = findById {
            it[AssignedUsers.sticky] = stickyId
            it[AssignedUsers.user] = userId
        }

        @IgnorableReturnValue
        fun new(
            sticky: StickyNoteEntity,
            user: UserEntity,
            due: Instant?,
        ) = new(
            CompositeID {
                it[AssignedUsers.sticky] = sticky.id
                it[AssignedUsers.user] = user.id
            },
        ) {
            this.sticky = sticky
            this.user = user
            this.due = due
        }
    }

    var sticky by StickyNoteEntity referencedOn AssignedUsers.sticky
    var user by UserEntity referencedOn AssignedUsers.user
    var due by AssignedUsers.due

    fun toModel() = AssignedUserModel(user.id.value, due)
}

class BoardUserEntity(
    id: EntityID<CompositeID>,
) : CompositeEntity(id) {
    companion object : CompositeEntityClass<BoardUserEntity>(BoardUsers) {
        fun findById(
            id: Uuid,
            userId: Uuid,
        ) = findById {
            it[BoardUsers.board] = id
            it[BoardUsers.user] = userId
        }

        @IgnorableReturnValue
        fun new(
            board: BoardEntity,
            user: UserEntity,
            role: Role,
        ) = new(
            CompositeID {
                it[BoardUsers.board] = board.id
                it[BoardUsers.user] = user.id
            },
        ) {
            this.board = board
            this.user = user
            this.role = role
        }

        @IgnorableReturnValue
        fun new(invite: InvitationEntity) =
            new(
                CompositeID {
                    it[BoardUsers.board] = invite.board.id
                    it[BoardUsers.user] = invite.receiver.id
                },
            ) {
                this.board = invite.board
                this.user = invite.receiver
                this.role = invite.role
            }
    }

    var board by BoardEntity referencedOn BoardUsers.board
    var user by UserEntity referencedOn BoardUsers.user
    var role by BoardUsers.role
    var lastViewedAt by BoardUsers.lastViewedAt

    fun toResponseModel() = BoardUserResponseModel(user.toSummaryModel(), role, lastViewedAt)
}

class InvitationEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<InvitationEntity>(Invitations)

    var board by BoardEntity referencedOn Invitations.board
    var sender by UserEntity referencedOn Invitations.sender
    var receiver by UserEntity referencedOn Invitations.receiver
    var role by Invitations.role

    fun toModel() = Invitation(id.value, board.id.value, sender.id.value, receiver.id.value, role)
}
