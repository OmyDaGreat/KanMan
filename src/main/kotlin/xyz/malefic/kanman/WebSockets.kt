package xyz.malefic.kanman

import co.touchlab.kermit.Logger
import org.http4k.routing.path
import org.http4k.routing.websocket.bind
import org.http4k.routing.websockets
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.StickyCreateModel
import xyz.malefic.kanman.data.StickyNoteEntity
import xyz.malefic.kanman.data.toModel
import xyz.malefic.kanman.data.transaction.isBoardValid
import xyz.malefic.kanman.util.ConnectionRegistry
import xyz.malefic.kanman.util.WsAbort
import xyz.malefic.kanman.util.abort
import xyz.malefic.kanman.util.authWS
import xyz.malefic.kanman.util.wsLens
import kotlin.uuid.Uuid

val ws =
    websockets(
        "/api/ws/{id}" bind
            authWS { user, request ->
                WsResponse { ws ->
                    try {
                        val id = Uuid.parse(request.path("id") ?: ws.abort("Missing board id"))
                        if (!isBoardValid(id, user)) {
                            ws.abort("Board not found or access denied")
                        }
                        ConnectionRegistry.register(id, ws)

                        ConnectionRegistry.broadcast(id, WsMessage("${user.username} has joined the board."))

                        ws.onMessage { msg ->
                            val stickyNoteRequest = wsLens<StickyCreateModel>(msg)
                            val stickyNote =
                                transaction {
                                    StickyNoteEntity.new {
                                        this.title = stickyNoteRequest.title
                                        this.content = stickyNoteRequest.content ?: ""
                                        this.column = stickyNoteRequest.column
                                        this.board = BoardEntity.findById(id) ?: ws.abort("Board not found")
                                    }
                                }.toModel()
                            ConnectionRegistry.broadcast(id, WsMessage("Sticky note created: $stickyNote."))
                        }

                        ws.onClose {
                            ConnectionRegistry.broadcast(id, WsMessage("${user.username} has left the board."))
                            ConnectionRegistry.unregister(id, ws)
                        }
                    } catch (_: WsAbort) {
                        // already sent error + closed
                    } catch (e: Exception) {
                        Logger.e(e, "WebSockets") { "Failed to create sticky note" }
                        ws.send(WsMessage("Failed to create sticky note"))
                        ws.close()
                    }
                }
            },
    )
