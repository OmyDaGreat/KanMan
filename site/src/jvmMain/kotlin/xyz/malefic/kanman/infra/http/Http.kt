package xyz.malefic.kanman.infra.http

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType
import org.http4k.routing.path
import xyz.malefic.kanman.api.util.json
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.features.auth.authenticate
import xyz.malefic.kanman.features.auth.authenticateOptional
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

fun api(handler: suspend Raise<Issue>.(Request) -> Response): HttpHandler =
    { request ->
        runBlocking {
            either { catch({ handler(request) }) { e: Throwable -> raise(Issue.Server.Internal.from(e)) } }.getOrElse {
                Logger.e(it, "HTTP") { "Internal server error" }
                it.toResponse()
            }
        }
    }

fun apiAuth(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> Response): HttpHandler =
    api { request -> handler(request.authenticate(), request) }

fun apiAuthOptional(handler: suspend Raise<Issue>.(UserResponseModel?, Request) -> Response): HttpHandler =
    api { request -> handler(request.authenticateOptional(), request) }

context(_: Raise<Issue>)
inline fun <reified T : Any> Request.model() =
    catch({ json.decodeFromString<T>(bodyString()) })
    { raise(Issue.Validation.BadRequest("Invalid JSON for request body: ${it.message}")) }

fun Issue.toResponse(): Response =
    when (this) {
        is Issue.Auth -> response(Status.UNAUTHORIZED, this)
        is Issue.Access.Forbidden, is Issue.Board.AccessDenied -> response(Status.FORBIDDEN, this)
        is Issue.Board.NotFound, is Issue.User.NotFound -> response(Status.NOT_FOUND, this)
        is Issue.User.AlreadyExists, is Issue.Server.Conflict -> response(Status.CONFLICT, this)
        is Issue.Board.InvalidId, is Issue.Validation.BadRequest -> response(Status.BAD_REQUEST, this)
        is Issue.Server.RateLimited -> response(Status.TOO_MANY_REQUESTS, this)
        is Issue.Server.Internal -> response(Status.INTERNAL_SERVER_ERROR, this)
        is Issue.Validation.BadResponse -> response(Status.INTERNAL_SERVER_ERROR, Issue.Server.Internal(message))
        is Issue.Client -> response(Status.INTERNAL_SERVER_ERROR, Issue.Server.Internal(message))
    }

fun response(status: Status) = Response.Companion(status)

inline fun <reified T : Any> response(
    status: Status,
    body: T,
) = Response.Companion(status).contentType(ContentType.APPLICATION_JSON).body(json.encodeToString(body))

fun rateLimit(
    requests: Int,
    windowMillis: Long,
): Filter {
    val hits = ConcurrentHashMap<String, MutableList<Long>>()
    return { next ->
        { request ->
            val ip = request.header("X-Forwarded-For") ?: request.source?.address ?: "unknown"
            val now = System.currentTimeMillis()
            val userHits = hits.getOrPut(ip) { mutableListOf() }

            synchronized(userHits) {
                either {
                    userHits.removeIf { it < now - windowMillis }
                    ensure(userHits.size < requests) { Issue.Server.RateLimited(userHits.first() + windowMillis - now) }
                    userHits.add(now)
                    next(request)
                }.getOrElse { it.toResponse() }
            }
        }
    }
}

context(_: Raise<Issue>)
fun Request.boardId(field: String = "id"): Uuid = ensureNotNull(path(field)?.let { Uuid.parseOrNull(it) }) { Issue.Board.InvalidId() }

fun Request.pagination() = (query("page")?.toIntOrNull() ?: 1) to (query("limit")?.toIntOrNull() ?: 50)

fun apiBoardAuth(
    field: String = "id",
    handler: suspend Raise<Issue>.(UserResponseModel, Uuid, Request) -> Response,
) = apiAuth { user, request -> handler(user, request.boardId(field), request) }
