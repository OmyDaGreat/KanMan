package xyz.malefic.kanman.auth

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.routing.bind
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.model.RefreshRequestModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.user.createUser
import xyz.malefic.kanman.util.authRequest
import xyz.malefic.kanman.util.catchPlus
import xyz.malefic.kanman.util.error
import xyz.malefic.kanman.util.model
import xyz.malefic.kanman.util.response

val authRoutes =
    listOf(
        "/api/ping" bind GET to { response(OK).body("pong") },
        "/api/health" bind GET to { response(OK).body("healthy") },
        "/api/user/register" bind POST to
            catchPlus("Failed to register user") {
                model<UserRequestModel> { _, user ->
                    transaction {
                        val userResult = createUser(user)
                        val tokens = issueTokenPair(userResult.entity)

                        response(OK, tokens)
                    }
                }
            },
        "/api/login" bind POST to
            catchPlus("Failed to process login") {
                model<UserRequestModel> { _, login ->
                    val tokens = getTokensFromLogin(login) ?: return@model error(UNAUTHORIZED) { "Invalid username or password" }

                    response(OK, tokens)
                }
            },
        "/api/logout" bind POST to
            model<RefreshRequestModel> { _, refresh ->
                revokeRefreshToken(refresh.refreshToken)

                response(OK)
            },
        "/api/token/refresh" bind POST to
            catchPlus("Failed to refresh tokens") {
                model<RefreshRequestModel> { _, refresh ->
                    val tokens =
                        refreshTokens(refresh.refreshToken) ?: return@model error(UNAUTHORIZED) { "Invalid or expired refresh token" }

                    response(OK, tokens)
                }
            },
        "/api/me" bind GET to
            authRequest { user ->
                response(OK, user)
            },
    )
