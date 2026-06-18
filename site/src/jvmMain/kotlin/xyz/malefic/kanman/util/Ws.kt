package xyz.malefic.kanman.util

import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsFilter
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.then
import xyz.malefic.kanman.auth.currentUser
import xyz.malefic.kanman.auth.getUserFromAccessToken
import xyz.malefic.kanman.auth.requestUser
import xyz.malefic.kanman.data.model.ErrorModel
import xyz.malefic.kanman.data.model.UserResponseModel

val authWS: WsFilter =
    WsFilter { next ->
        { request ->
            val token =
                request
                    .header("Authorization")
                    ?.takeIf { it.startsWith("Bearer ") }
                    ?.removePrefix("Bearer ")
                    ?.trim()
            if (token.isNullOrBlank()) {
                WsResponse { ws ->
                    ws.send(WsMessage("Error: Missing bearer token"))
                    ws.close()
                }
            } else {
                val user = getUserFromAccessToken(token)
                if (user == null) {
                    WsResponse { ws ->
                        ws.send(WsMessage("Error: Invalid or expired token"))
                        ws.close()
                    }
                } else {
                    next(request.with(requestUser of user.id))
                }
            }
        }
    }

fun authWS(next: (UserResponseModel, Request) -> WsResponse) =
    authWS.then { request ->
        next(
            currentUser(request) ?: return@then WsResponse { ws ->
                ws.send(WsMessage("Error: Authenticated user not found"))
                ws.close()
            },
            request,
        )
    }

class WsAbort(
    override val message: String,
    override val cause: Exception? = null,
) : Exception(message, cause)

fun abortWS(
    message: String,
    cause: Exception? = null,
): Nothing = throw WsAbort(message, cause)

inline fun <reified T : Any> wsLens(msg: WsMessage) = WsMessage.auto<T>().toLens()(msg)

inline fun <reified T : Any> wsLens(obj: T) = WsMessage.auto<T>().toLens()(obj)

inline fun <reified T : Any> Websocket.send(obj: T) = send(wsLens(obj))

fun Websocket.error(message: String) = send(ErrorModel(message))
