package xyz.malefic.kanman.server.features.board

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.select
import xyz.malefic.kanman.server.data.AssignedUserEntity
import xyz.malefic.kanman.server.data.AssignedUsers
import xyz.malefic.kanman.server.data.BoardEntity
import xyz.malefic.kanman.server.data.BoardEvents
import xyz.malefic.kanman.server.data.BoardUserEntity
import xyz.malefic.kanman.server.data.Boards
import xyz.malefic.kanman.server.data.StickyNoteEntity
import xyz.malefic.kanman.server.data.StickyNotes
import xyz.malefic.kanman.server.data.data
import xyz.malefic.kanman.server.data.entity
import xyz.malefic.kanman.server.infra.ws.Registry
import xyz.malefic.kanman.shared.data.model.BoardAction
import xyz.malefic.kanman.shared.data.model.BoardAction.DELETE_BOARD
import xyz.malefic.kanman.shared.data.model.BoardAction.EDIT_STICKY
import xyz.malefic.kanman.shared.data.model.BoardAction.INVITE_USER
import xyz.malefic.kanman.shared.data.model.BoardAction.VIEW_BOARD
import xyz.malefic.kanman.shared.data.model.BoardCreateModel
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Board.AccessDenied
import xyz.malefic.kanman.shared.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.shared.data.model.PaginatedResponse
import xyz.malefic.kanman.shared.data.model.Role
import xyz.malefic.kanman.shared.data.model.RoleUpdateRequest
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.Visibility.PRIVATE
import xyz.malefic.kanman.shared.data.model.Visibility.PUBLIC
import kotlin.reflect.KProperty1
import kotlin.time.Clock
import kotlin.uuid.Uuid

context(_: Raise<Issue>, _: JdbcTransaction)
private fun fetchBoard(
    id: Uuid,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = ensureNotNull(BoardEntity.find { Boards.id eq id }.with(*relations).firstOrNull())
    { Issue.Board.NotFound() }

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

context(user: UserResponseModel, _: Raise<Issue>, _: JdbcTransaction)
infix fun Uuid.perform(action: BoardAction) = user.getAccessibleBoard(this, action)

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
fun getBoard(
    id: Uuid,
    action: BoardAction = VIEW_BOARD,
    user: UserResponseModel? = null,
) = user.data {
    getAccessibleBoard(
        id,
        action,
        BoardEntity::owner,
        BoardEntity::stickies,
        BoardEntity::memberships,
        StickyNoteEntity::assignedUsers,
        AssignedUserEntity::user,
    ).toResponseModel()
}

infix fun UserResponseModel.create(boardCreateModel: BoardCreateModel) =
    data {
        val createdBoard =
            BoardEntity.new {
                title = boardCreateModel.title
                description = boardCreateModel.description
                visibility = boardCreateModel.visibility
                owner = entity
            }
        BoardUserEntity.new(createdBoard, entity, Role.OWNER)
        createdBoard.toResponseModel()
    }

context(_: Raise<Issue>)
infix fun UserResponseModel.delete(boardId: Uuid) =
    data {
        (boardId perform DELETE_BOARD).delete()
        Registry.closeAll(boardId)
    }

context(_: Raise<Issue>)
infix fun UserResponseModel.join(boardId: Uuid) =
    data {
        val board = boardId perform VIEW_BOARD

        ensure(board.visibility == PUBLIC) { AccessDenied("You cannot join a private board without an invitation") }

        if (board.memberships.none { it.user.id.value == id }) {
            BoardUserEntity.new(board, entity, Role.GUEST)
        }

        board.toResponseModel()
    }

infix fun UserResponseModel.historyOf(targetId: Uuid) = BoardHistoryBuilder(this, targetId)

class BoardHistoryBuilder(
    val actor: UserResponseModel,
    val targetId: Uuid,
) {
    context(_: Raise<Issue>)
    infix fun with(pagination: Pair<Int, Int>) = actor.getHistory(targetId, pagination.first, pagination.second)
}

context(_: Raise<Issue>)
fun UserResponseModel.getHistory(
    boardId: Uuid,
    page: Int = 1,
    limit: Int = 50,
) = data {
    val board = boardId perform EDIT_STICKY
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
infix fun UserResponseModel.getUsers(id: Uuid) = data { getAccessibleBoard(id, VIEW_BOARD).memberships.map { it.user.toSummaryModel() } }

infix fun UserResponseModel.kick(targetId: Uuid) = KickBuilder(this, targetId)

class KickBuilder(
    val actor: UserResponseModel,
    val targetId: Uuid,
) {
    context(_: Raise<Issue>)
    infix fun from(boardId: Uuid) = actor.kick(boardId, targetId)
}

context(_: Raise<Issue>)
fun UserResponseModel.kick(
    boardId: Uuid,
    targetId: Uuid,
) = data {
    val board = boardId perform INVITE_USER

    ensure(board.owner.id.value != targetId) {
        if (id == targetId) {
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
) = data {
    val board = boardId perform DELETE_BOARD

    ensure(board.owner.id.value == id) { AccessDenied("Only the board owner can change the owner") }
    ensure(board.owner.id.value != targetId) { AccessDenied("User is already the board owner") }

    val target = ensureNotNull(board.memberships.firstOrNull { it.user.id.value == targetId }) { Issue.User.NotFound() }
    val user = ensureNotNull(board.memberships.firstOrNull { it.user.id.value == id }) { Issue.User.NotFound() }

    user.role = Role.ADMIN
    target.role = Role.OWNER
    target.lastViewedAt = Clock.System.now()
    board.owner = target.user
}

infix fun UserResponseModel.update(targetId: Uuid) = UpdateRoleBuilder(this, targetId)

class UpdateRoleBuilder(
    val actor: UserResponseModel,
    val targetId: Uuid,
    val boardId: Uuid? = null,
) {
    context(_: Raise<Issue>)
    infix fun to(request: RoleUpdateRequest) =
        actor.update(ensureNotNull(boardId) { BadRequest("Missing board context") }, targetId, request.role)

    infix fun from(id: Uuid) = UpdateRoleBuilder(actor, targetId, id)
}

context(_: Raise<Issue>)
fun UserResponseModel.update(
    boardId: Uuid,
    targetId: Uuid,
    role: Role,
) = data {
    if (role == Role.OWNER) return@data changeOwner(boardId, targetId)
    val board = boardId perform INVITE_USER
    val target = ensureNotNull(board.memberships.firstOrNull { it.user.id.value == targetId }) { Issue.User.NotFound() }
    ensure(target.role != Role.OWNER) { AccessDenied("A board owner cannot be changed") }
    target.role = role
}

context(_: Raise<Issue>)
infix fun UserResponseModel?.publicBoardsWith(pagination: Pair<Int, Int> = 1 to 50) =
    data {
        val query = Boards.select(Boards.columns).where { Boards.visibility eq PUBLIC }
        val total = query.count()
        val items =
            BoardEntity
                .wrapRows(query.offset((pagination.first - 1L) * pagination.second).limit(pagination.second))
                .with(BoardEntity::owner, BoardEntity::memberships)
                .map { it.toSummaryModel(this?.id) }

        PaginatedResponse(items, pagination.first, pagination.second, total)
    }
