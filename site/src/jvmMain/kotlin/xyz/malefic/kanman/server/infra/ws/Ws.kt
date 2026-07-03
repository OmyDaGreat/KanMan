package xyz.malefic.kanman.server.infra.ws

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.bind
import arrow.core.raise.either
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import xyz.malefic.kanman.server.features.auth.authenticate
import xyz.malefic.kanman.server.infra.http.getId
import xyz.malefic.kanman.shared.api.util.json
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Server.Internal
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import kotlin.uuid.Uuid

fun apiWS(handler: suspend Raise<Issue>.(Request) -> WsResponse): (Request) -> WsResponse =
    { request ->
        runBlocking {
            either { catch({ handler(request) }) { e: Throwable -> raise(Internal.from(e)) } }.getOrElse { error ->
                WsResponse.Companion { ws ->
                    ws.send(error)
                    ws.close()
                }
            }
        }
    }

fun apiAuthWS(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> WsResponse) =
    apiWS { request -> handler(request.authenticate(), request) }

context(_: Raise<Issue>)
inline fun <reified T : Any> WsMessage.model() =
    Either
        .catch { json.decodeFromString<T>(bodyString()) }
        .mapLeft { Issue.Validation.BadRequest("Invalid JSON for request body: ${it.message}") }
        .bind()

inline fun <reified T : Any> Websocket.send(obj: T) = send(WsMessage(json.encodeToString(obj)))

fun apiBoardAuthWS(
    field: String = "id",
    handler: suspend Raise<Issue>.(UserResponseModel, Uuid, Request) -> WsResponse,
) = apiAuthWS { user, request -> handler(user, request.getId(field), request) }
