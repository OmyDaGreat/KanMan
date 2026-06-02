package xyz.malefic.kanman.data.transaction

import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardCreateModel
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.BoardUsers
import xyz.malefic.kanman.data.UserResponseModel
import xyz.malefic.kanman.data.Visibility.PRIVATE
import xyz.malefic.kanman.util.ConnectionRegistry
import xyz.malefic.kanman.util.error
import kotlin.uuid.Uuid

fun createBoard(
    boardCreateModel: BoardCreateModel,
    user: UserResponseModel,
): BoardEntity =
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
        createdBoard
    }

fun deleteBoard(
    id: Uuid,
    user: UserResponseModel,
): Response? =
    transaction {
        val board = BoardEntity.findById(id) ?: return@transaction Response(NOT_FOUND).with("Board not found".error)
        if (board.owner.id != user.id) {
            return@transaction Response(FORBIDDEN).with("User is not the owner for the board".error)
        }
        board.delete()
        ConnectionRegistry.closeAll(id)
        null
    }

fun isBoardValid(
    id: Uuid,
    user: UserResponseModel? = null,
): Boolean =
    transaction {
        val board = BoardEntity.findById(id) ?: return@transaction false
        return@transaction !(board.visibility == PRIVATE && user != null && board.users.none { it.id.value == user.id })
    }
