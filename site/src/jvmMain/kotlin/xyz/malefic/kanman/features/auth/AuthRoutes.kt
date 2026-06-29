package xyz.malefic.kanman.features.auth

import arrow.core.raise.ensureNotNull
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.infra.http.api
import xyz.malefic.kanman.infra.http.apiAuth
import xyz.malefic.kanman.infra.http.model
import xyz.malefic.kanman.infra.http.response

val authRoutes =
    arrayOf(
        "/api/register" bind POST to
            api { request ->
                val tokens = request.model<UserRequestModel>().create()

                response(OK, tokens.response).withRefreshCookie(tokens.refreshToken)
            },
        "/api/login" bind POST to
            api { request ->
                val tokens = getTokensFromLogin(request.model())

                response(OK, tokens.response).withRefreshCookie(tokens.refreshToken)
            },
        "/api/logout" bind POST to
            api { request ->
                request.cookie("refresh_token")?.value?.let { revokeRefreshToken(it) }

                response(OK).invalidateCookie("refresh_token")
            },
        "/api/token/refresh" bind POST to
            api { request ->
                val tokens = refreshTokens(ensureNotNull(request.cookie("refresh_token")?.value) { Issue.Auth.MissingToken() })

                response(OK, tokens.response).withRefreshCookie(tokens.refreshToken)
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
