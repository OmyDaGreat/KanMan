package xyz.malefic.kanman

import org.http4k.routing.path
import org.http4k.routing.websocket.bind
import org.http4k.routing.websockets
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.StickyNoteEntity
import xyz.malefic.kanman.data.stickyCreateLens
import xyz.malefic.kanman.data.toModel
import xyz.malefic.kanman.util.WsAbort
import xyz.malefic.kanman.util.abort
import xyz.malefic.kanman.util.authWS
import kotlin.uuid.Uuid

val ws =
    websockets(
        "/api/ws/{id}" bind
            authWS { user, request ->
                WsResponse { ws ->
                    ws.send(WsMessage("hello ${user.username}."))

                    ws.onMessage { msg ->
                        try {
                            val id = Uuid.parse(request.path("id") ?: ws.abort("Missing board {id} query param"))
                            val stickyNoteRequest = stickyCreateLens(msg)
                            ws.send(WsMessage("${user.username} is adding a sticky note!"))
                            val stickyNote =
                                transaction {
                                    StickyNoteEntity.new {
                                        this.title = stickyNoteRequest.title
                                        this.content = stickyNoteRequest.content
                                        this.column = stickyNoteRequest.column
                                        this.board = BoardEntity.findById(id) ?: ws.abort("Board not found")
                                    }
                                }.toModel()
                            ws.send(WsMessage("Sticky note created: $stickyNote"))
                        } catch (_: WsAbort) {
                            // already sent error + closed
                        } catch (e: Exception) {
                            ws.send(WsMessage("Failed to create sticky note: $e"))
                            ws.close()
                        }
                    }

                    ws.onClose { println("${user.username} is leaving the board.") }
                }
            },
    )
