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
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.db.Users
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Auth.MissingToken
import xyz.malefic.kanman.data.model.Issue.Board.AccessDenied
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PRIVATE
import xyz.malefic.kanman.data.model.Visibility.PUBLIC
import xyz.malefic.kanman.util.ConnectionRegistry
import kotlin.uuid.Uuid

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

context(r: Raise<Issue>)
fun deleteBoard(
    id: Uuid,
    user: UserResponseModel,
) = transaction {
    val board = ensureNotNull(BoardEntity.findById(id)) { Issue.Board.NotFound() }

    ensure(board.owner.id.value == user.id) { AccessDenied("You don't have permission to delete this board") }

    board.delete()
    ConnectionRegistry.closeAll(id)
}

context(r: Raise<Issue>)
fun inviteToBoard(
    boardId: Uuid,
    inviteUserId: Uuid,
    addUserId: Uuid,
) = transaction {
    val board = ensureNotNull(BoardEntity.findById(boardId)) { Issue.Board.NotFound() }

    ensure(board.owner.id.value == inviteUserId || (board.visibility == PUBLIC && board.users.any { it.id.value == inviteUserId }))
    { AccessDenied("You don't have permission to invite users") }

    val addUser = ensureNotNull(UserEntity.find { Users.id eq addUserId }.firstOrNull()) { Issue.User.NotFound() }

    BoardUsers.insertIgnore {
        it[BoardUsers.user] = addUser.id
        it[BoardUsers.board] = board.id
    }

    (board.users.map { it.toResponseModel() } + addUser.toResponseModel()).distinct()
}

context(r: Raise<Issue>)
fun getBoards(
    visibility: Visibility?,
    user: UserResponseModel?,
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
