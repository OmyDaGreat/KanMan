package xyz.malefic.kanman.http

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.bind
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.UserEntity
import xyz.malefic.kanman.data.boardLens
import xyz.malefic.kanman.data.boardRequestLens
import xyz.malefic.kanman.data.error
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.toModel
import xyz.malefic.kanman.data.userLens
import xyz.malefic.kanman.util.auth
import xyz.malefic.kanman.util.model

val post =
    arrayOf(
        "/api/user/register" bind Method.POST to
            auth REQUEST@{ _, user ->
                // TODO: More proper user creds validation (+ password hashing)
                val userResult =
                    try {
                        transaction {
                            UserEntity.new {
                                this.username = user.username
                                this.password = user.password
                            }
                        }
                    } catch (e: Exception) {
                        return@REQUEST Response(Status.BAD_REQUEST).with(errorLens of "Failed to create user: $e".error)
                    }

                Response(Status.OK).with(userLens of userResult.toModel())
            },
        "/api/board/create" bind Method.POST to
            model(boardRequestLens) REQUEST@{ _, board ->
                val board =
                    try {
                        transaction {
                            BoardEntity.new {
                                this.title = board.title
                                this.visibility = board.visibility
                            }
                        }
                    } catch (e: Exception) {
                        return@REQUEST Response(Status.BAD_REQUEST).with(errorLens of "Failed to create board: $e".error)
                    }

                Response(Status.OK).with(boardLens of board.toModel())
            },
    )
