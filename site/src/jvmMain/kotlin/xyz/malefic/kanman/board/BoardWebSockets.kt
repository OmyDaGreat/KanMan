package xyz.malefic.kanman.board

import arrow.core.raise.catch
import arrow.core.raise.either
import co.touchlab.kermit.Logger
import org.http4k.routing.websocket.bind
import org.http4k.routing.websockets
import org.http4k.websocket.WsResponse
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.auth.getUserSummary
import xyz.malefic.kanman.data.model.Issue.Server.Internal
import xyz.malefic.kanman.data.model.WsAction
import xyz.malefic.kanman.data.model.WsEvent.AssignedUser
import xyz.malefic.kanman.data.model.WsEvent.StickyCreated
import xyz.malefic.kanman.data.model.WsEvent.StickyDeleted
import xyz.malefic.kanman.data.model.WsEvent.StickyMoved
import xyz.malefic.kanman.data.model.WsEvent.UnassignedUser
import xyz.malefic.kanman.data.model.WsEvent.UserJoin
import xyz.malefic.kanman.data.model.WsEvent.UserLeave
import xyz.malefic.kanman.util.ConnectionRegistry
import xyz.malefic.kanman.util.model
import xyz.malefic.kanman.util.send

val boardWs =
    websockets(
        "/api/ws/{id}" bind
            apiBoardAuthWS { user, id, _ ->
                transaction { user.getAccessibleBoard(id) }
                val userSummary = user.toSummaryModel()

                WsResponse { ws ->
                    either {
                        catch({
                            ConnectionRegistry.register(id, ws)
                            ConnectionRegistry.broadcast(id, UserJoin(userSummary, id))
                        }) { raise(Internal(it.message ?: "Connection registry failure")) }

                        ws.onMessage { msg ->
                            either {
                                val event =
                                    when (val action = msg.model<WsAction>()) {
                                        is WsAction.StickyCreate -> {
                                            val sticky = user.createSticky(action, id)
                                            StickyCreated(userSummary, sticky)
                                        }

                                        is WsAction.StickyMove -> {
                                            user.moveSticky(action, id)
                                            StickyMoved(userSummary, action.stickyId, action.newColumn)
                                        }

                                        is WsAction.StickyDelete -> {
                                            user.deleteSticky(action, id)
                                            StickyDeleted(userSummary, action.stickyId)
                                        }

                                        is WsAction.AssignUser -> {
                                            user.assignUser(action, id)
                                            AssignedUser(userSummary, action.stickyId, getUserSummary(action.userId))
                                        }

                                        is WsAction.UnassignUser -> {
                                            user.unassignUser(action, id)
                                            UnassignedUser(userSummary, action.stickyId, getUserSummary(action.userId))
                                        }
                                    }

                                ConnectionRegistry.broadcast(id, event)
                            }.onLeft { e ->
                                Logger.e(e, "WebSockets") { "Failed to handle message" }
                                ws.send(e)
                            }
                        }

                        ws.onClose {
                            if (ConnectionRegistry.unregister(id, ws)) {
                                ConnectionRegistry.broadcast(id, UserLeave(userSummary, id))
                            }
                        }

                        ws.onError { e ->
                            Logger.e(e, "WebSockets") { "${user.username} disconnected with error" }
                            if (ConnectionRegistry.unregister(id, ws)) {
                                ConnectionRegistry.broadcast(id, UserLeave(userSummary, id))
                            }
                        }
                    }.onLeft { issue ->
                        Logger.e(issue, "WebSockets") { "Error during WS setup" }
                        ws.send(issue)
                        ws.close()
                    }
                }
            },
    )
