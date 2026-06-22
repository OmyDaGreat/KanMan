package xyz.malefic.kanman.util

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
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
import xyz.malefic.kanman.data.model.Issue.Server.BadRequest
import xyz.malefic.kanman.data.model.Issue.Server.Conflict
import xyz.malefic.kanman.data.model.Issue.Server.Forbidden
import xyz.malefic.kanman.data.model.Issue.Server.Internal
import xyz.malefic.kanman.data.model.Issue.Server.NotFound
import xyz.malefic.kanman.data.model.Issue.Server.RateLimited
import xyz.malefic.kanman.data.model.Issue.Server.Unauthorized
import xyz.malefic.kanman.data.model.UserResponseModel
import java.util.concurrent.ConcurrentHashMap

fun api(handler: suspend Raise<Issue>.(Request) -> Response): HttpHandler =
    { request ->
        runBlocking {
            either {
                Either
                    .catch { handler(request) }
                    .mapLeft {
                        Logger.e(it, "HTTP") { "Internal server error" }
                        raise(Internal(it.message ?: "Internal server error"))
                    }.bind()
            }.fold({ it.toResponse() }, { it })
        }
    }

fun apiAuth(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> Response): HttpHandler =
    { request -> runBlocking { either { handler(authenticate(request).bind(), request) }.fold({ it.toResponse() }, { it }) } }

context(r: Raise<Issue>)
inline fun <reified T : Any> Request.model(): T =
    Either
        .catch { lens<T>()(this) }
        .mapLeft { BadRequest("Invalid JSON for request body: ${it.message}") }
        .bind()

fun Issue.toResponse(): Response =
    when (this) {
        is Unauthorized -> response(UNAUTHORIZED, this)
        is NotFound -> response(NOT_FOUND, this)
        is Internal -> response(INTERNAL_SERVER_ERROR, this)
        is RateLimited -> response(TOO_MANY_REQUESTS, this)
        is BadRequest -> response(BAD_REQUEST, this)
        is Conflict -> response(CONFLICT, this)
        is Forbidden -> response(FORBIDDEN, this)
        is Issue.Client -> response(INTERNAL_SERVER_ERROR, Internal(message))
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
                    ensure(userHits.size < requests) { RateLimited("Rate limit exceeded. Try again later.") }
                    userHits.add(now)
                    next(request)
                }.fold({ it.toResponse() }, { it })
            }
        }
    }
}
