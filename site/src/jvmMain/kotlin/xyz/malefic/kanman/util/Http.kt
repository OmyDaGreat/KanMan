package xyz.malefic.kanman.util

import arrow.core.merge
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import xyz.malefic.kanman.auth.authenticate
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Auth
import xyz.malefic.kanman.data.model.UserResponseModel
import java.util.concurrent.ConcurrentHashMap

fun api(handler: suspend Raise<Issue>.(Request) -> Response): HttpHandler =
    { request ->
        runBlocking {
            either {
                handler(request)
            }.mapLeft {
                Logger.e(it, "HTTP") { "Internal server error" }
                Issue.Server.Internal(it.message).toResponse()
            }.merge()
        }
    }

fun apiAuth(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> Response): HttpHandler =
    { request -> runBlocking { either { handler(authenticate(request), request) }.mapLeft { it.toResponse() }.merge() } }

context(_: Raise<Issue>)
inline fun <reified T : Any> Request.model() =
    catch({ lens<T>()(this) })
    { raise(Issue.Validation.BadRequest("Invalid JSON for request body: ${it.message}")) }

fun Issue.toResponse(): Response =
    when (this) {
        is Auth -> response(UNAUTHORIZED, this)
        is Issue.Access.Forbidden, is Issue.Board.AccessDenied -> response(FORBIDDEN, this)
        is Issue.Board.NotFound, is Issue.User.NotFound -> response(NOT_FOUND, this)
        is Issue.User.AlreadyExists, is Issue.Server.Conflict -> response(CONFLICT, this)
        is Issue.Board.InvalidId, is Issue.Validation.BadRequest -> response(BAD_REQUEST, this)
        is Issue.Server.RateLimited -> response(TOO_MANY_REQUESTS, this)
        is Issue.Server.Internal -> response(INTERNAL_SERVER_ERROR, this)
        is Issue.Validation.BadResponse -> response(INTERNAL_SERVER_ERROR, Issue.Server.Internal(message))
        is Issue.Client -> response(INTERNAL_SERVER_ERROR, Issue.Server.Internal(message))
    }

inline fun <reified T : Any> lens() = Body.auto<T>().toLens()

fun response(status: Status) = Response(status)

inline fun <reified T : Any> response(
    status: Status,
    body: T,
) = Response(status).with(lens<T>().of(body))

fun rateLimit(
    requests: Int,
    windowMillis: Long,
): Filter {
    val hits = ConcurrentHashMap<String, MutableList<Long>>()
    return { next ->
        { request ->
            val ip = request.header("X-Forwarded-For") ?: request.source?.address ?: "unknown"
            val now = nowMs()
            val userHits = hits.getOrPut(ip) { mutableListOf() }

            synchronized(userHits) {
                either {
                    userHits.removeIf { it < now - windowMillis }
                    ensure(userHits.size < requests) { Issue.Server.RateLimited(userHits.first() + windowMillis - now) }
                    userHits.add(now)
                    next(request)
                }.mapLeft { it.toResponse() }.merge()
            }
        }
    }
}
