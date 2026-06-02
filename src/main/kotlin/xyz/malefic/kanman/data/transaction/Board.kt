package xyz.malefic.kanman.data.transaction

import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardCreateModel
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.BoardUsers
import xyz.malefic.kanman.data.UserResponseModel

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
