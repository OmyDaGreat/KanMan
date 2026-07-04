package xyz.malefic.kanman.server.infra.http

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType
import org.http4k.routing.path
import xyz.malefic.kanman.server.features.auth.authenticate
import xyz.malefic.kanman.server.features.auth.authenticateOptional
import xyz.malefic.kanman.shared.api.util.json
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
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

fun Issue.toResponse(): Response {
    val status =
        when (this) {
            is Issue.Auth -> Status.UNAUTHORIZED
            is Issue.Access.Forbidden, is Issue.Board.AccessDenied -> Status.FORBIDDEN
            is Issue.Board.NotFound, is Issue.User.NotFound -> Status.NOT_FOUND
            is Issue.User.AlreadyExists, is Issue.Server.Conflict -> Status.CONFLICT
            is Issue.Board.InvalidId, is Issue.Validation.BadRequest, is Issue.User.InvalidUser -> Status.BAD_REQUEST
            is Issue.Server.RateLimited -> Status.TOO_MANY_REQUESTS
            is Issue.Server.Internal, is Issue.Validation.BadResponse, is Issue.Client -> Status.INTERNAL_SERVER_ERROR
        }
    val body = if (this is Issue.Validation.BadResponse || this is Issue.Client) Issue.Server.Internal(message) else this
    return response<Issue>(status, body)
}

fun response(status: Status) = Response(status)

inline fun <reified T : Any> response(
    status: Status,
    body: T,
) = response(status).contentType(APPLICATION_JSON).body(json.encodeToString(body))

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
                    ensure(userHits.size < requests) { Issue.Server.RateLimited((userHits.first() + windowMillis - now).milliseconds) }
                    userHits.add(now)
                    next(request)
                }.getOrElse { it.toResponse() }
            }
        }
    }
}

context(_: Raise<Issue>)
fun Request.getId(field: String = "id"): Uuid = ensureNotNull(path(field)?.let { Uuid.parseOrNull(it) }) { Issue.Board.InvalidId() }

fun Request.pagination() = (query("page")?.toIntOrNull() ?: 1).coerceAtLeast(1) to (query("limit")?.toIntOrNull() ?: 50).coerceIn(1, 100)

fun apiId(
    field: String = "id",
    handler: suspend Raise<Issue>.(Uuid, Request) -> Response,
) = api { request -> handler(request.getId(field), request) }

fun apiIdAuth(
    field: String = "id",
    handler: suspend Raise<Issue>.(UserResponseModel, Uuid, Request) -> Response,
) = apiAuth { user, request -> handler(user, request.getId(field), request) }
