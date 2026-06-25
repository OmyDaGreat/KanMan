package xyz.malefic.kanman.util

import arrow.core.Either
import arrow.core.merge
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.bind
import arrow.core.raise.either
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import xyz.malefic.kanman.auth.authenticate
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Server.Internal
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.json

fun apiWS(handler: suspend Raise<Issue>.(Request) -> WsResponse): (Request) -> WsResponse =
    { request ->
        runBlocking {
            either {
                catch({ handler(request) })
                { e: Throwable -> if (e is Issue) raise(e) else raise(Internal(e.message ?: "Internal server error")) }
            }.mapLeft { error ->
                WsResponse { ws ->
                    ws.send(error)
                    ws.close()
                }
            }.merge()
        }
    }

fun apiAuthWS(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> WsResponse) =
    apiWS { request -> handler(authenticate(request), request) }

context(_: Raise<Issue>)
inline fun <reified T : Any> WsMessage.model() =
    Either.catch { json.decodeFromString<T>(bodyString()) }.mapLeft { BadRequest("Invalid JSON for request body: ${it.message}") }.bind()

inline fun <reified T : Any> Websocket.send(obj: T) = send(WsMessage(json.encodeToString(obj)))
