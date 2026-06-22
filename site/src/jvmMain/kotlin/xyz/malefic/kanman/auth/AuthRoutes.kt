package xyz.malefic.kanman.auth

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.model.RefreshRequestModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.util.api
import xyz.malefic.kanman.util.apiAuth
import xyz.malefic.kanman.util.model
import xyz.malefic.kanman.util.response

val authRoutes =
    arrayOf(
        "/api/register" bind POST to
            api { request ->
                val user = request.model<UserRequestModel>()
                transaction {
                    val userResult = user.create()
                    val tokens = issueTokenPair(userResult.entity)

                    response(OK, tokens)
                }
            },
        "/api/login" bind POST to
            api { request ->
                val login = request.model<UserRequestModel>()
                val tokens = getTokensFromLogin(login)

                response(OK, tokens)
            },
        "/api/logout" bind POST to
            api { request ->
                val refresh = request.model<RefreshRequestModel>()
                revokeRefreshToken(refresh.refreshToken)

                response(OK)
            },
        "/api/token/refresh" bind POST to
            api { request ->
                val refresh = request.model<RefreshRequestModel>()
                val tokens = refreshTokens(refresh.refreshToken)

                response(OK, tokens)
            },
        "/api/me" bind GET to
            apiAuth { user, _ ->
                response(OK, user)
            },
    )
