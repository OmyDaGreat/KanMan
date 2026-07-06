package xyz.malefic.kanman.server.features.auth

import arrow.core.raise.ensureNotNull
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.routing.bind
import xyz.malefic.kanman.server.infra.http.api
import xyz.malefic.kanman.server.infra.http.model
import xyz.malefic.kanman.server.infra.http.rateLimit
import xyz.malefic.kanman.server.infra.http.response
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.UserRequestModel
import kotlin.time.Duration.Companion.minutes

val throttler = rateLimit(10, 1.minutes.inWholeMilliseconds)

val authRoutes =
    arrayOf(
        "/api/auth/password/strength" bind POST to
            api { request ->
                response(OK, request.model<String>().strength())
            },
        "/api/auth/register" bind POST to
            throttler(
                api { request ->
                    val tokens = request.model<UserRequestModel>().create()

                    response(OK, tokens.response) withCookie tokens.refreshToken
                },
            ),
        "/api/auth/login" bind POST to
            throttler(
                api { request ->
                    val tokens = getTokensFromLogin(request.model())

                    response(OK, tokens.response) withCookie tokens.refreshToken
                },
            ),
        "/api/auth/logout" bind POST to
            throttler(
                api { request ->
                    request.cookie("refresh_token")?.value?.let { revokeRefreshToken(it) }

                    response(OK).invalidateCookie("refresh_token")
                },
            ),
        "/api/auth/token/refresh" bind POST to
            throttler(
                api { request ->
                    val tokens = refreshTokens(ensureNotNull(request.cookie("refresh_token")?.value) { Issue.Auth.MissingToken() })

                    response(OK, tokens.response) withCookie tokens.refreshToken
                },
            ),
    )
