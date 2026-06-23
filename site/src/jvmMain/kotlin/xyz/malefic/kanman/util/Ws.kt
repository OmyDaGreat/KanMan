package xyz.malefic.kanman.util

import arrow.core.Either
import arrow.core.merge
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
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
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.data.model.UserResponseModel

fun apiWS(handler: suspend Raise<Issue>.(Request) -> WsResponse): (Request) -> WsResponse =
    { request ->
        runBlocking {
            val result: Either<Issue, WsResponse> =
                try {
                    either { handler(this, request) }
                } catch (e: Exception) {
                    Either.Left(Internal(e.message ?: "Internal server error"))
                }
            result
                .mapLeft { error ->
                    WsResponse { ws ->
                        ws.send(error)
                        ws.close()
                    }
                }.merge()
        }
    }

fun apiAuthWS(handler: suspend Raise<Issue>.(UserResponseModel, Request) -> WsResponse) =
    apiWS { request -> handler(authenticate(request), request) }

context(r: Raise<Issue>)
inline fun <reified T : Any> WsMessage.model() =
    Either.catch { WsMessage.auto<T>().toLens()(this) }.mapLeft { BadRequest("Invalid JSON for request body: ${it.message}") }.bind()

inline fun <reified T : Any> Websocket.send(obj: T) = send(WsMessage.auto<T>().toLens()(obj))

fun Websocket.error(message: String = "Internal server error") = send(Internal(message))

inline fun <reified T : Issue> Websocket.error(error: T) = send(error)
