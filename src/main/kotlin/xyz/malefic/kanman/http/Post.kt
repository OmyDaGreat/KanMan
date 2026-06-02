package xyz.malefic.kanman.http

import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.with
import org.http4k.routing.bind
import xyz.malefic.kanman.data.BoardCreateModel
import xyz.malefic.kanman.data.RefreshRequestModel
import xyz.malefic.kanman.data.UserRequestModel
import xyz.malefic.kanman.data.boardLens
import xyz.malefic.kanman.data.tokenResponseLens
import xyz.malefic.kanman.data.transaction.createBoard
import xyz.malefic.kanman.data.transaction.createUser
import xyz.malefic.kanman.data.transaction.getTokensFromLogin
import xyz.malefic.kanman.data.transaction.refreshTokens
import xyz.malefic.kanman.data.userResponseLens
import xyz.malefic.kanman.util.auth
import xyz.malefic.kanman.util.catchPlus
import xyz.malefic.kanman.util.error
import xyz.malefic.kanman.util.model

val post =
    arrayOf(
        "/api/login" bind POST to
            catchPlus("Failed to process login") {
                model<UserRequestModel> MODEL@{ _, login ->
                    val tokens =
                        getTokensFromLogin(login) ?: return@MODEL Response(UNAUTHORIZED).with("Invalid username or password".error)

                    Response(OK).with(tokenResponseLens of tokens)
                }
            },
        "/api/token/refresh" bind POST to
            catchPlus("Failed to refresh tokens") {
                model<RefreshRequestModel> MODEL@{ _, refresh ->
                    val tokens =
                        refreshTokens(refresh.refreshToken)
                            ?: return@MODEL Response(UNAUTHORIZED).with("Invalid or expired refresh token".error)

                    Response(OK).with(tokenResponseLens of tokens)
                }
            },
        "/api/user/register" bind POST to
            catchPlus("Failed to register user") {
                model<UserRequestModel> { _, user ->
                    val userResult = createUser(user)

                    Response(OK).with(userResponseLens of userResult)
                }
            },
        "/api/board" bind POST to
            catchPlus("Failed to create board") {
                auth<BoardCreateModel> { user, boardRequest ->
                    val boardResponse = createBoard(boardRequest, user)

                    Response(OK).with(boardLens of boardResponse)
                }
            },
    )
