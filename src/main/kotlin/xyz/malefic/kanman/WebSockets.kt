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
                        val id = Uuid.parse(request.path("id") ?: ws.abort("Missing board {id} query param"))
                        if (!isBoardValid(id, user)) {
                            ws.abort("Board not found or access denied")
                        }
                        ConnectionRegistry.register(id, ws)

                        ws.send(WsMessage("hello ${user.username}."))

                        ws.onMessage { msg ->
                            val stickyNoteRequest = wsLens<StickyCreateModel>(msg)
                            ws.send(WsMessage("${user.username} is adding a sticky note!"))
                            val stickyNote =
                                transaction {
                                    StickyNoteEntity.new {
                                        this.title = stickyNoteRequest.title
                                        this.content = stickyNoteRequest.content ?: ""
                                        this.column = stickyNoteRequest.column
                                        this.board = BoardEntity.findById(id) ?: ws.abort("Board not found")
                                    }
                                }.toModel()
                            ws.send(WsMessage("Sticky note created: $stickyNote"))
                        }

                        ws.onClose {
                            ConnectionRegistry.unregister(id, ws)
                            Logger.i(tag = "WebSockets") { "${user.username} is leaving the board with id $id." }
                        }
                    } catch (_: WsAbort) {
                        // already sent error + closed
                    } catch (e: Exception) {
                        ws.send(WsMessage("Failed to create sticky note: $e"))
                        ws.close()
                    }
                }
            },
    )
