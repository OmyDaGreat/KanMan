package xyz.malefic.kanman.features.board

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.db.AssignedUserEntity
import xyz.malefic.kanman.data.db.AssignedUsers
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.BoardEvents
import xyz.malefic.kanman.data.db.BoardUserEntity
import xyz.malefic.kanman.data.db.BoardUsers
import xyz.malefic.kanman.data.db.Boards
import xyz.malefic.kanman.data.db.InvitationEntity
import xyz.malefic.kanman.data.db.Invitations
import xyz.malefic.kanman.data.db.StickyNoteEntity
import xyz.malefic.kanman.data.db.StickyNotes
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.model.BoardAction
import xyz.malefic.kanman.data.model.BoardAction.DELETE_BOARD
import xyz.malefic.kanman.data.model.BoardAction.EDIT_STICKY
import xyz.malefic.kanman.data.model.BoardAction.INVITE_USER
import xyz.malefic.kanman.data.model.BoardAction.VIEW_BOARD
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Auth.MissingToken
import xyz.malefic.kanman.data.model.Issue.Board.AccessDenied
import xyz.malefic.kanman.data.model.PaginatedResponse
import xyz.malefic.kanman.data.model.Role
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PRIVATE
import xyz.malefic.kanman.data.model.Visibility.PUBLIC
import xyz.malefic.kanman.data.model.WsAction
import xyz.malefic.kanman.features.auth.entity
import xyz.malefic.kanman.infra.ws.Registry
import kotlin.reflect.KProperty1
import kotlin.time.Clock
import kotlin.uuid.Uuid

context(_: Raise<Issue>, _: JdbcTransaction)
private fun fetchBoard(
    id: Uuid,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = ensureNotNull(
    BoardEntity.find { Boards.id eq id }.with(*relations).firstOrNull(),
) { Issue.Board.NotFound() }

context(_: Raise<AccessDenied>, _: JdbcTransaction)
private fun UserResponseModel?.permissionCheck(
    board: BoardEntity,
    action: BoardAction,
) {
    val userRole = board.memberships.find { it.user.id.value == this?.id }

    if (board.visibility == PRIVATE) {
        ensure(userRole != null) { AccessDenied("You don't have permission to access this board") }
    }

    val role = userRole?.role ?: Role.GUEST

    ensure(role.permission(action)) { AccessDenied("You don't have permission to perform this action") }
}

context(_: Raise<Issue>, _: JdbcTransaction)
fun UserResponseModel?.getAccessibleBoard(
    id: Uuid,
    action: BoardAction = VIEW_BOARD,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = fetchBoard(id, BoardEntity::memberships, BoardUserEntity::user, *relations).also { board ->
    permissionCheck(board, action)
    this?.let { user -> BoardUserEntity.findById(board.id.value, user.id)?.lastViewedAt = Clock.System.now() }
}

context(_: Raise<Issue>)
fun getAccessibleBoard(
    id: Uuid,
    action: BoardAction = VIEW_BOARD,
    user: UserResponseModel? = null,
) = transaction {
    user
        .getAccessibleBoard(
            id,
            action,
            BoardEntity::owner,
            BoardEntity::stickies,
            BoardEntity::memberships,
            StickyNoteEntity::assignedUsers,
            AssignedUserEntity::user,
        ).toResponseModel()
}

fun UserResponseModel.createBoard(boardCreateModel: BoardCreateModel) =
    transaction {
        val createdBoard =
            BoardEntity.new {
                title = boardCreateModel.title
                visibility = boardCreateModel.visibility
                owner = this@createBoard.entity
            }
        BoardUserEntity.new(createdBoard, this@createBoard.entity, Role.OWNER)
        createdBoard.toResponseModel()
    }

context(_: Raise<Issue>)
fun UserResponseModel.deleteBoard(id: Uuid) =
    transaction {
        getAccessibleBoard(id, DELETE_BOARD).delete()
        Registry.closeAll(id)
    }

context(_: Raise<Issue>)
fun UserResponseModel.getBoardHistory(
    id: Uuid,
    page: Int = 1,
    limit: Int = 50,
) = transaction {
    val board = getAccessibleBoard(id, VIEW_BOARD)
    val total = board.history.count()
    val items =
        board
            .history
            .orderBy(BoardEvents.timestamp to SortOrder.DESC)
            .offset((page - 1L) * limit)
            .limit(limit)
            .map { it.toModel() }

    PaginatedResponse(items, page, limit, total)
}

context(_: Raise<Issue>)
fun UserResponseModel.getBoardUsers(id: Uuid) =
    transaction { getAccessibleBoard(id, VIEW_BOARD).memberships.map { it.user.toSummaryModel() } }

context(_: Raise<Issue>)
fun UserResponseModel.getInvites() =
    transaction {
        InvitationEntity.find { Invitations.receiver eq this@getInvites.id }.map { it.toModel() }
    }

context(_: Raise<Issue>)
fun UserResponseModel.invite(
    boardId: Uuid,
    inviteRequest: InviteRequest,
) = transaction {
    ensure(inviteRequest.role != Role.OWNER) { AccessDenied("A board owner cannot be invited") }

    val board = getAccessibleBoard(boardId, INVITE_USER)
    val addUser = ensureNotNull(UserEntity.findById(inviteRequest.userId)) { Issue.User.NotFound() }

    InvitationEntity.new {
        this.board = board
        sender = this@invite.entity
        receiver = addUser
        role = inviteRequest.role
    }
}

context(_: Raise<Issue>)
fun UserResponseModel.acceptInvite(inviteId: Uuid) =
    transaction {
        val invite = ensureNotNull(InvitationEntity.findById(inviteId)) { Issue.Board.NotFound() }

        ensure(invite.receiver.id.value == this@acceptInvite.id) { AccessDenied("You are not invited to this board") }

        BoardUserEntity.new(invite)
        (invite.board.memberships.map { it.user } + invite.receiver).map { it.toSummaryModel() }.distinct()
    }

context(_: Raise<Issue>)
fun UserResponseModel.declineInvite(inviteId: Uuid) =
    transaction {
        val invite = ensureNotNull(InvitationEntity.findById(inviteId)) { Issue.Board.NotFound() }
        ensure(invite.receiver.id.value == this@declineInvite.id) { AccessDenied("You are not invited to this board") }
        invite.delete()
    }

context(_: Raise<Issue>)
fun UserResponseModel.kick(
    boardId: Uuid,
    targetId: Uuid,
) = transaction {
    val board = getAccessibleBoard(boardId, INVITE_USER)

    ensure(board.owner.id.value != targetId) {
        if (this@kick.id == targetId) {
            AccessDenied("Please specify another board owner before leaving")
        } else {
            AccessDenied("A board owner cannot be uninvited")
        }
    }

    BoardUserEntity.findById(board.id.value, targetId)?.delete() ?: raise(Issue.Board.NotFound())

    AssignedUserEntity
        .wrapRows(
            (AssignedUsers innerJoin StickyNotes)
                .select(AssignedUsers.columns)
                .where { (AssignedUsers.user eq targetId) and (StickyNotes.board eq boardId) },
        ).forEach { it.delete() }

    board.memberships.filter { it.user.id.value != targetId }.map { it.user.toSummaryModel() }
}

context(_: Raise<Issue>, _: JdbcTransaction)
private fun UserResponseModel.changeOwner(
    boardId: Uuid,
    targetId: Uuid,
) {
    val board = getAccessibleBoard(boardId, DELETE_BOARD)

    ensure(board.owner.id.value == this@changeOwner.id) { AccessDenied("Only the board owner can change the owner") }
    ensure(board.owner.id.value != targetId) { AccessDenied("User is already the board owner") }

    val target = ensureNotNull(board.memberships.firstOrNull { it.user.id.value == targetId }) { Issue.User.NotFound() }
    val user = ensureNotNull(board.memberships.firstOrNull { it.user.id.value == this@changeOwner.id }) { Issue.User.NotFound() }

    user.role = Role.ADMIN
    target.role = Role.OWNER
    target.lastViewedAt = Clock.System.now()
    board.owner = target.user
}

context(_: Raise<Issue>)
fun UserResponseModel.updateUserRole(
    boardId: Uuid,
    targetId: Uuid,
    role: Role,
) = transaction {
    if (role == Role.OWNER) return@transaction changeOwner(boardId, targetId)
    val board = getAccessibleBoard(boardId, INVITE_USER)
    val target = ensureNotNull(board.memberships.firstOrNull { it.user.id.value == targetId }) { Issue.User.NotFound() }
    ensure(target.role != Role.OWNER) { AccessDenied("A board owner cannot be changed") }
    target.role = role
}

context(_: Raise<Issue>)
fun getBoards(
    user: UserResponseModel? = null,
    visibility: Visibility? = null,
    page: Int = 1,
    limit: Int = 50,
) = transaction {
    val query =
        when (visibility) {
            PUBLIC -> {
                Boards.select(Boards.columns).where { Boards.visibility eq PUBLIC }
            }

            PRIVATE -> {
                ensureNotNull(user) { MissingToken() }
                (Boards innerJoin BoardUsers)
                    .select(Boards.columns)
                    .where { (Boards.visibility eq PRIVATE) and (BoardUsers.user eq user.id) }
            }

            else -> {
                if (user == null) {
                    Boards.select(Boards.columns).where { Boards.visibility eq PUBLIC }
                } else {
                    Boards.select(Boards.columns).where {
                        (Boards.visibility eq PUBLIC) or
                            (Boards.id inSubQuery BoardUsers.select(BoardUsers.board).where { BoardUsers.user eq user.id })
                    }
                }
            }
        }

    val total = query.count()

    val items =
        BoardEntity
            .wrapRows(query.offset((page - 1L) * limit).limit(limit))
            .with(BoardEntity::owner, BoardEntity::memberships)
            .map { it.toSummaryModel(user?.id) }

    PaginatedResponse(items, page, limit, total)
}

context(_: Raise<Issue>)
fun UserResponseModel.createSticky(
    event: WsAction.StickyCreate,
    boardId: Uuid,
) = transaction {
    val board = getAccessibleBoard(boardId, EDIT_STICKY)

    StickyNoteEntity
        .new {
            title = event.title
            content = event.content ?: ""
            column = event.column
            this.board = board
        }.toModel()
}

context(_: Raise<Issue>)
fun UserResponseModel.deleteSticky(
    event: WsAction.StickyDelete,
    boardId: Uuid,
) = transaction {
    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == getAccessibleBoard(boardId, EDIT_STICKY) }?.delete())
    { Issue.Board.NotFound() }
}

context(_: Raise<Issue>)
fun UserResponseModel.moveSticky(
    event: WsAction.StickyMove,
    boardId: Uuid,
) = transaction {
    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == getAccessibleBoard(boardId, EDIT_STICKY) })
        { Issue.Board.NotFound() }.column = event.newColumn
}

context(_: Raise<Issue>)
fun UserResponseModel.assignUser(
    event: WsAction.AssignUser,
    boardId: Uuid,
): Unit =
    transaction {
        val board = getAccessibleBoard(boardId, EDIT_STICKY)
        val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }
        val user = ensureNotNull(UserEntity.findById(event.userId)) { Issue.User.NotFound() }
        val old = AssignedUserEntity.findById(sticky.id.value, user.id.value)
        if (old != null) old.due = event.due else AssignedUserEntity.new(sticky, user, event.due)
    }

context(_: Raise<Issue>)
fun UserResponseModel.unassignUser(
    event: WsAction.UnassignUser,
    boardId: Uuid,
) = transaction {
    val board = getAccessibleBoard(boardId, EDIT_STICKY)
    val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }
    val user = ensureNotNull(UserEntity.findById(event.userId)) { Issue.User.NotFound() }
    AssignedUserEntity.findById(sticky.id.value, user.id.value)?.delete() ?: raise(Issue.Board.NotFound())
}
