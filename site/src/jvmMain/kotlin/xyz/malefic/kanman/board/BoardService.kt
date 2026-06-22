package xyz.malefic.kanman.board

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.auth.entity
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.BoardUsers
import xyz.malefic.kanman.data.db.Boards
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardModel
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PRIVATE
import xyz.malefic.kanman.util.ConnectionRegistry
import kotlin.uuid.Uuid

fun createBoard(
    boardCreateModel: BoardCreateModel,
    user: UserResponseModel,
): BoardModel =
    transaction {
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

fun deleteBoard(
    id: Uuid,
    user: UserResponseModel,
) = transaction {
    val board = BoardEntity.findById(id) ?: return@transaction false
    if (board.owner.id.value != user.id) {
        return@transaction false
    }
    board.delete()
    ConnectionRegistry.closeAll(id)
    true
}

fun isBoardValid(
    id: Uuid,
    user: UserResponseModel? = null,
) = transaction {
    val board = BoardEntity.findById(id) ?: return@transaction false
    return@transaction !(board.visibility == PRIVATE && user != null && board.users.none { it.id.value == user.id })
}

fun getBoards(
    visibility: Visibility?,
    user: UserResponseModel?,
): List<BoardSummaryModel>? =
    transaction {
        when (visibility) {
            Visibility.PUBLIC -> {
                BoardEntity
                    .find { Boards.visibility eq Visibility.PUBLIC }
                    .map { it.toSummaryModel() }
            }

            PRIVATE -> {
                if (user == null) return@transaction null
                BoardEntity
                    .find { Boards.visibility eq PRIVATE }
                    .filter { board -> board.users.any { u -> u.id.value == user.id } }
                    .map { it.toSummaryModel() }
            }

            else -> {
                val public =
                    BoardEntity
                        .find { Boards.visibility eq Visibility.PUBLIC }
                        .map { it.toSummaryModel() }
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
