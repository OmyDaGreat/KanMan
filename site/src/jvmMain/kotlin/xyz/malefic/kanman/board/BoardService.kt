package xyz.malefic.kanman.board

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
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
import xyz.malefic.kanman.data.model.WsEvent
import xyz.malefic.kanman.util.ConnectionRegistry
import kotlin.uuid.Uuid

context(_: Raise<Issue>)
fun getBoard(
    id: Uuid,
    user: UserResponseModel? = null,
) = transaction {
    ensureNotNull(BoardEntity.findById(id)) { Issue.Board.NotFound() }.toModel().also { board ->
        ensure(board.visibility == PUBLIC || board.users.any { it.id == user?.id })
        { AccessDenied("You don't have permission to view this board") }
    }
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
    val board = ensureNotNull(BoardEntity.findById(id)) { Issue.Board.NotFound() }

    ensure(board.owner.id.value == user.id) { AccessDenied("You don't have permission to delete this board") }

    board.delete()
    ConnectionRegistry.closeAll(id)
}

context(_: Raise<Issue>)
fun getBoardUsers(
    id: Uuid,
    user: UserResponseModel,
) = transaction { getBoard(id, user).users }

context(_: Raise<Issue>)
fun UserResponseModel.inviteToBoard(
    boardId: Uuid,
    inviteRequest: InviteRequest,
) = transaction {
    val board = ensureNotNull(BoardEntity.findById(boardId)) { Issue.Board.NotFound() }

    ensure(
        board.owner.id.value == this@inviteToBoard.id ||
            (board.visibility == PUBLIC && board.users.any { it.id.value == this@inviteToBoard.id }),
    )
    { AccessDenied("You don't have permission to invite users") }

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
    when (visibility) {
        PUBLIC -> {
            BoardEntity.find { Boards.visibility eq PUBLIC }.map { it.toSummaryModel() }
        }

        PRIVATE -> {
            ensureNotNull(user) { MissingToken() }
            BoardEntity
                .find { Boards.visibility eq PRIVATE }
                .filter { board -> board.users.any { u -> u.id.value == user.id } }
                .map { it.toSummaryModel() }
        }

        else -> {
            val public = BoardEntity.find { Boards.visibility eq PUBLIC }.map { it.toSummaryModel() }
            if (user == null) {
                public
            } else {
                val privateVisible =
                    BoardEntity
                        .find { Boards.visibility eq PRIVATE }
                        .filter { board -> board.users.any { u -> u.id.value == user.id } }
                        .map { it.toSummaryModel() }
                (public + privateVisible).distinctBy { it.id }
            }
        }
    }
}

context(_: Raise<Issue>)
fun UserResponseModel.createSticky(
    event: WsAction.StickyCreate,
    boardId: Uuid,
) = transaction {
    val board = ensureNotNull(BoardEntity.findById(boardId)) { Issue.Board.NotFound() }

    ensure(board.visibility == PUBLIC || board.users.any { it.id.value == this@createSticky.id })
    { AccessDenied("You don't have permission to view this board") }

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
    val board = ensureNotNull(BoardEntity.findById(boardId)) { Issue.Board.NotFound() }

    ensure(board.visibility == PUBLIC || board.users.any { it.id.value == this@deleteSticky.id })
    { AccessDenied("You don't have permission to view this board") }

    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }?.delete()) { Issue.Board.NotFound() }
}

context(_: Raise<Issue>)
fun UserResponseModel.moveSticky(
    event: WsAction.StickyMove,
    boardId: Uuid,
) = transaction {
    val board = ensureNotNull(BoardEntity.findById(boardId)) { Issue.Board.NotFound() }
    val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }

    ensure(board.visibility == PUBLIC || board.users.any { it.id.value == this@moveSticky.id })
    { AccessDenied("You don't have permission to view this board") }

    sticky.column = event.newColumn
}
