package xyz.malefic.kanman.util

import org.http4k.core.Request
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.then
import xyz.malefic.kanman.data.UserResponseModel
import xyz.malefic.kanman.data.transaction.currentUser

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

class WsAbort : Throwable()

fun Websocket.abort(error: String): Nothing {
    send(WsMessage("Error: $error"))
    close()
    throw WsAbort()
}

inline fun <reified T : Any> wsLens() = WsMessage.auto<T>().toLens()
