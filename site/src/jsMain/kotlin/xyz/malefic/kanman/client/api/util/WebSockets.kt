package xyz.malefic.kanman.client.api.util

import co.touchlab.kermit.Logger
import kotlinx.browser.window
import org.w3c.dom.WebSocket
import xyz.malefic.kanman.client.api.util.AuthSession.accessToken
import xyz.malefic.kanman.shared.data.model.BoardResponseModel

object WebSockets {
    val wsBaseUrl: String
        get() {
            val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
            return "$protocol//${window.location.host}"
        }
    val BoardResponseModel.ws
        get() =
            WebSocket("$wsBaseUrl/api/ws/$id?token=$accessToken").also { ws ->
                ws.onmessage = {
                    Logger.d(tag = "WebSocket") { it.data as String }
                }
            }
}
