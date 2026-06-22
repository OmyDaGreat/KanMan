package xyz.malefic.kanman.board

import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import co.touchlab.kermit.Logger
import org.http4k.routing.path
import org.http4k.routing.websocket.bind
import org.http4k.routing.websockets
import org.http4k.websocket.WsResponse
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.StickyNoteEntity
import xyz.malefic.kanman.data.model.Issue.Server.BadRequest
import xyz.malefic.kanman.data.model.Issue.Server.Internal
import xyz.malefic.kanman.data.model.Issue.Server.NotFound
import xyz.malefic.kanman.data.model.WsEvent.StickyCreate
import xyz.malefic.kanman.data.model.WsEvent.UserJoin
import xyz.malefic.kanman.data.model.WsEvent.UserLeave
import xyz.malefic.kanman.util.ConnectionRegistry
import xyz.malefic.kanman.util.apiAuthWS
import xyz.malefic.kanman.util.error
import xyz.malefic.kanman.util.wsLens
import kotlin.uuid.Uuid

val boardWs =
    websockets(
        "/api/ws/{id}" bind
            apiAuthWS { user, request ->
                WsResponse { ws ->
                    try {
                        val id = ensureNotNull(request.path("id")?.let { Uuid.parseOrNull(it) }) { BadRequest("Invalid board id") }
                        ensure(isBoardValid(id, user)) { NotFound("Board not found or access denied") }

                        ConnectionRegistry.register(id, ws)
                        ConnectionRegistry.broadcast(id, UserJoin(id, user))

                        ws.onMessage { msg ->
                            try {
                                val stickyNoteRequest = wsLens<StickyCreate.Model>(msg)
                                val stickyNote =
                                    transaction {
                                        StickyNoteEntity
                                            .new {
                                                this.title = stickyNoteRequest.title
                                                this.content = stickyNoteRequest.content ?: ""
                                                this.column = stickyNoteRequest.column
                                                this.board = BoardEntity.findById(id) ?: throw Exception("Board not found")
                                            }.toModel()
                                    }
                                ConnectionRegistry.broadcast(id, StickyCreate(id, user, stickyNote))
                            } catch (e: Exception) {
                                Logger.e(e, "WebSockets") { "Failed to create sticky note" }
                                ws.error("Internal server error")
                            }
                        }

                        ws.onClose {
                            if (ConnectionRegistry.unregister(id, ws)) {
                                ConnectionRegistry.broadcast(id, UserLeave(id, user))
                            }
                        }

                        ws.onError { e ->
                            Logger.e(e, "WebSockets") { "${user.username} disconnected with error on board $id" }
                            if (ConnectionRegistry.unregister(id, ws)) {
                                ConnectionRegistry.broadcast(id, UserLeave(id, user))
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(e, "WebSockets") { "Error during WS setup" }
                        ws.error(Internal(e.message ?: "Internal server error"))
                        ws.close()
                    }
                }
            },
    )
