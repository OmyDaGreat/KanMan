package xyz.malefic.kanman.board

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SizedCollection
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.auth.entity
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.BoardUsers
import xyz.malefic.kanman.data.db.Boards
import xyz.malefic.kanman.data.db.StickyNoteEntity
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.db.Users
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Auth.MissingToken
import xyz.malefic.kanman.data.model.Issue.Board.AccessDenied
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PRIVATE
import xyz.malefic.kanman.data.model.Visibility.PUBLIC
import xyz.malefic.kanman.data.model.WsAction
import xyz.malefic.kanman.util.ConnectionRegistry
import kotlin.reflect.KProperty1
import kotlin.uuid.Uuid

context(_: Raise<Issue>, _: JdbcTransaction)
private fun fetchBoard(
    id: Uuid,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = ensureNotNull(
    BoardEntity.find { Boards.id eq id }.with(*relations).firstOrNull(),
) { Issue.Board.NotFound() }

context(_: Raise<AccessDenied>, _: JdbcTransaction)
private fun UserResponseModel?.viewCheck(board: BoardEntity) {
    ensure(board.visibility == PUBLIC || board.users.any { it.id.value == this?.id })
    { AccessDenied("You don't have permission to view this board") }
}

context(_: Raise<Issue>, _: JdbcTransaction)
fun UserResponseModel?.getAccessibleBoard(
    id: Uuid,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = fetchBoard(id, BoardEntity::users, *relations).also { viewCheck(it) }

context(_: Raise<Issue>)
fun getBoard(
    id: Uuid,
    user: UserResponseModel? = null,
) = transaction {
    user
        .getAccessibleBoard(
            id,
            BoardEntity::owner,
            BoardEntity::stickies,
            StickyNoteEntity::assignedUsers,
        ).toModel()
}

fun createBoard(
    boardCreateModel: BoardCreateModel,
    user: UserResponseModel,
) = transaction {
    val createdBoard =
        BoardEntity.new {
            title = boardCreateModel.title
            visibility = boardCreateModel.visibility
            owner = user.entity
        }
    BoardUsers.insert {
        it[BoardUsers.user] = user.id
        it[BoardUsers.board] = createdBoard.id
    }
    createdBoard.toModel()
}

context(_: Raise<Issue>)
fun deleteBoard(
    id: Uuid,
    user: UserResponseModel,
) = transaction {
    val board = fetchBoard(id, BoardEntity::owner)

    ensure(board.owner.id.value == user.id) { AccessDenied("You don't have permission to delete this board") }

    board.delete()
    ConnectionRegistry.closeAll(id)
}

context(_: Raise<Issue>)
fun getBoardUsers(
    id: Uuid,
    user: UserResponseModel,
) = transaction { user.getAccessibleBoard(id).users.map { it.toSummaryModel() } }

context(_: Raise<Issue>)
fun UserResponseModel.inviteToBoard(
    boardId: Uuid,
    inviteRequest: InviteRequest,
) = transaction {
    val board = getAccessibleBoard(boardId)

    ensure(board.users.any { it.id.value == this@inviteToBoard.id })
    { AccessDenied("You don't have permission to invite users to this board") }

    val addUser = ensureNotNull(UserEntity.find { Users.id eq inviteRequest.userId }.firstOrNull()) { Issue.User.NotFound() }

    BoardUsers.insertIgnore {
        it[BoardUsers.user] = addUser.id
        it[BoardUsers.board] = board.id
    }

    (board.users + addUser).map { it.toSummaryModel() }.distinct()
}

context(_: Raise<Issue>)
fun getBoards(
    user: UserResponseModel? = null,
    visibility: Visibility? = null,
) = transaction {
    val query =
        when (visibility) {
            PUBLIC -> {
                BoardEntity.find { Boards.visibility eq PUBLIC }
            }

            PRIVATE -> {
                ensureNotNull(user) { MissingToken() }
                BoardEntity.wrapRows(
                    (Boards innerJoin BoardUsers)
                        .select(Boards.columns)
                        .where { (Boards.visibility eq PRIVATE) and (BoardUsers.user eq user.id) },
                )
            }

            else -> {
                if (user == null) {
                    BoardEntity.find { Boards.visibility eq PUBLIC }
                } else {
                    BoardEntity.wrapRows(
                        Boards
                            .join(BoardUsers, JoinType.LEFT, Boards.id, BoardUsers.board)
                            .select(Boards.columns)
                            .where { Boards.visibility eq PUBLIC or ((Boards.visibility eq PRIVATE) and (BoardUsers.user eq user.id)) },
                    )
                }
            }
        }

    query.with(BoardEntity::owner).distinctBy { it.id }.map { it.toSummaryModel() }
}

context(_: Raise<Issue>)
fun UserResponseModel.createSticky(
    event: WsAction.StickyCreate,
    boardId: Uuid,
) = transaction {
    val board = getAccessibleBoard(boardId)

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
    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == getAccessibleBoard(boardId) }?.delete())
    { Issue.Board.NotFound() }
}

context(_: Raise<Issue>)
fun UserResponseModel.moveSticky(
    event: WsAction.StickyMove,
    boardId: Uuid,
) = transaction {
    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == getAccessibleBoard(boardId) })
        { Issue.Board.NotFound() }.column = event.newColumn
}

context(_: Raise<Issue>)
fun UserResponseModel.assignUser(
    event: WsAction.AssignUser,
    boardId: Uuid,
) = transaction {
    val board = getAccessibleBoard(boardId)
    val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }
    val user = ensureNotNull(UserEntity.findById(event.userId)) { Issue.User.NotFound() }
    sticky.assignedUsers = SizedCollection(sticky.assignedUsers + user)
}

context(_: Raise<Issue>)
fun UserResponseModel.unassignUser(
    event: WsAction.UnassignUser,
    boardId: Uuid,
) = transaction {
    val board = getAccessibleBoard(boardId)
    val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }
    val user = ensureNotNull(UserEntity.findById(event.userId)) { Issue.User.NotFound() }
    sticky.assignedUsers = SizedCollection(sticky.assignedUsers - user)
}
