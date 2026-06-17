package xyz.malefic.kanman.data.transaction

import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.model.BoardModel
import xyz.malefic.kanman.data.BoardUsers
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.Visibility.PRIVATE
import xyz.malefic.kanman.data.model.toModel
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
    if (board.owner.id != user.id) {
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
