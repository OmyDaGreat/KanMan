package xyz.malefic.kanman.http

import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.with
import org.http4k.routing.bind
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.BoardUsers
import xyz.malefic.kanman.data.UserEntity
import xyz.malefic.kanman.data.boardCreateLens
import xyz.malefic.kanman.data.boardLens
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.errorModel
import xyz.malefic.kanman.data.refreshRequestLens
import xyz.malefic.kanman.data.toModel
import xyz.malefic.kanman.data.toResponseModel
import xyz.malefic.kanman.data.tokenResponseLens
import xyz.malefic.kanman.data.userRequestLens
import xyz.malefic.kanman.data.userResponseLens
import xyz.malefic.kanman.util.auth
import xyz.malefic.kanman.util.getTokensFromLogin
import xyz.malefic.kanman.util.hashPassword
import xyz.malefic.kanman.util.model
import xyz.malefic.kanman.util.refreshTokens

val post =
    arrayOf(
        "/api/login" bind POST to
            model(userRequestLens) REQUEST@{ _, login ->
                val tokens =
                    getTokensFromLogin(login)
                        ?: return@REQUEST Response(UNAUTHORIZED).with(errorLens of "Invalid username or password".errorModel)

                Response(OK).with(tokenResponseLens of tokens)
            },
        "/api/token/refresh" bind POST to
            model(refreshRequestLens) REQUEST@{ _, refresh ->
                val tokens =
                    refreshTokens(refresh.refreshToken)
                        ?: return@REQUEST Response(UNAUTHORIZED).with(errorLens of "Invalid or expired refresh token".errorModel)

                Response(OK).with(tokenResponseLens of tokens)
            },
        "/api/user/register" bind POST to
            model(userRequestLens) REQUEST@{ _, user ->
                val userResult =
                    try {
                        transaction {
                            UserEntity.new {
                                this.username = user.username
                                this.hashedPassword = hashPassword(user.password)
                            }
                        }
                    } catch (e: Exception) {
                        return@REQUEST Response(BAD_REQUEST).with(errorLens of "Failed to create user: $e".errorModel)
                    }

                Response(OK).with(userResponseLens of userResult.toResponseModel())
            },
        "/api/board/create" bind POST to
            auth(boardCreateLens) REQUEST@{ user, boardRequest ->
                val boardResponse =
                    try {
                        transaction {
                            val createdBoard =
                                BoardEntity.new {
                                    title = boardRequest.title
                                    visibility = boardRequest.visibility
                                }
                            BoardUsers.insert {
                                it[BoardUsers.user] = user.id
                                it[BoardUsers.board] = createdBoard.id
                            }
                            createdBoard
                        }
                    } catch (e: Exception) {
                        return@REQUEST Response(BAD_REQUEST).with(errorLens of "Failed to create board: $e".errorModel)
                    }

                Response(OK).with(boardLens of boardResponse.toModel())
            },
    )
