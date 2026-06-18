package xyz.malefic.kanman.user

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.auth.hashPassword
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.Boards
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PRIVATE
import xyz.malefic.kanman.data.model.Visibility.PUBLIC

fun createUser(user: UserRequestModel) =
    transaction {
        UserEntity.new {
            this.username = user.username
            this.hashedPassword = hashPassword(user.password)
        }
    }.toResponseModel()

fun getUserBoards(
    visibility: Visibility?,
    user: UserResponseModel?,
): List<BoardSummaryModel>? =
    transaction {
        when (visibility) {
            PUBLIC -> {
                BoardEntity
                    .find { Boards.visibility eq PUBLIC }
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
                        .find { Boards.visibility eq PUBLIC }
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
