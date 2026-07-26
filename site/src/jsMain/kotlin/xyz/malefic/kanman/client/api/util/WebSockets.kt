package xyz.malefic.kanman.client.api.util

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import co.touchlab.kermit.Logger
import kotlinx.browser.window
import org.w3c.dom.WebSocket
import xyz.malefic.kanman.client.api.util.AuthSession.accessToken
import xyz.malefic.kanman.shared.api.util.json
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.WsEvent
import kotlin.uuid.Uuid

object WebSockets {
    val log = Logger.withTag("WebSocket")
    val wsBaseUrl: String
        get() {
            val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
            return "$protocol//${window.location.host}"
        }

    fun connect(
        id: Uuid,
        onMessage: (Either<Issue, WsEvent>) -> Unit,
    ) = WebSocket("$wsBaseUrl/api/ws/$id?token=$accessToken").also { ws ->
        ws.onopen = { log.d { "WebSocket opened" } }
        ws.onmessage = {
            val data = it.data.toString()
            val result: Either<Issue, WsEvent> =
                either {
                    catch({ json.decodeFromString<WsEvent>(data) }) { _ ->
                        val issue =
                            catch({ json.decodeFromString<Issue>(data) }) { e ->
                                raise(Issue.Validation.BadResponse("Failed to decode: $e") as Issue)
                            }
                        raise(issue)
                    }
                }
            onMessage(result)
        }
        ws.onerror = { log.e { "WebSocket error" } }
        ws.onclose = { log.d { "WebSocket closed" } }
    }
}
