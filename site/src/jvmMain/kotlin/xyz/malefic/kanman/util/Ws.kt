package xyz.malefic.kanman.util

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import xyz.malefic.kanman.auth.authenticate
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Server.Internal
import xyz.malefic.kanman.data.model.UserResponseModel

fun apiWS(handler: suspend Raise<Issue>.(Request) -> WsResponse): (Request) -> WsResponse =
    { request ->
        runBlocking {
            either {
                Either.catch { handler(request) }.mapLeft { Internal(it.message ?: "Internal server error") }.bind()
            }.fold(
                { error ->
                    WsResponse { ws ->
                        ws.send(error)
                        ws.close()
                    }
                },
                { it },
            )
        }
    }

fun apiAuthWS(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> WsResponse): (Request) -> WsResponse =
    apiWS { request ->
        val user = authenticate(request).bind()
        handler(user, request)
    }

inline fun <reified T : Any> wsLens(msg: WsMessage) = WsMessage.auto<T>().toLens()(msg)

inline fun <reified T : Any> wsLens(obj: T) = WsMessage.auto<T>().toLens()(obj)

inline fun <reified T : Any> Websocket.send(obj: T) = send(wsLens(obj))

fun Websocket.error(message: String) = send(Internal(message))

inline fun <reified T : Issue> Websocket.error(error: T) = send(error)
