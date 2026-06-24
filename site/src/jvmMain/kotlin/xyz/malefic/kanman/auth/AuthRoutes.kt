package xyz.malefic.kanman.auth

import arrow.core.raise.ensureNotNull
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
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
                val tokens = request.model<UserRequestModel>().create()

                response(OK, tokens)
            },
        "/api/login" bind POST to
            api { request ->
                val login = request.model<UserRequestModel>()
                val tokens = getTokensFromLogin(login)

                response(OK, tokens)
            },
        "/api/logout" bind POST to
            api { request ->
                revokeRefreshToken(request.model<RefreshRequestModel>().refreshToken)

                response(OK)
            },
        "/api/token/refresh" bind POST to
            api { request ->
                val refresh = request.model<RefreshRequestModel>()
                val tokens = refreshTokens(refresh.refreshToken)

                response(OK, tokens)
            },
        "/api/users/{username}" bind GET to
            api { request ->
                response(OK, getUserSummary(ensureNotNull(request.path("username")) { BadRequest("Missing username") }))
            },
        "/api/me" bind GET to
            apiAuth { user, _ ->
                response(OK, user)
            },
    )
