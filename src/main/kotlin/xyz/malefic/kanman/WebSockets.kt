package xyz.malefic.kanman

import org.http4k.core.Request
import org.http4k.routing.path
import org.http4k.routing.websocket.bind
import org.http4k.routing.websockets
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse

val ws =
    websockets(
        "/{name}" bind { request: Request ->
            WsResponse { ws: Websocket ->
                val name = request.path("name")
                ws.send(WsMessage("hello $name"))
                ws.onMessage {
                    ws.send(WsMessage("$name is responding"))
                }
                ws.onClose { println("$name is closing") }
            }
        },
    )
