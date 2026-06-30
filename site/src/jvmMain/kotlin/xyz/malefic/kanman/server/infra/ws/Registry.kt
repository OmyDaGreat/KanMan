package xyz.malefic.kanman.server.infra.ws

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.http4k.websocket.Websocket
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.server.data.db.BoardEntity
import xyz.malefic.kanman.server.data.db.BoardEventEntity
import xyz.malefic.kanman.server.data.db.UserEntity
import xyz.malefic.kanman.shared.data.model.WsEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

object Registry {
    val connections = ConcurrentHashMap<Uuid, MutableSet<Websocket>>()

    fun register(
        boardId: Uuid,
        ws: Websocket,
    ) = connections.getOrPut(boardId) { ConcurrentHashMap.newKeySet() }.add(ws)

    inline fun <reified T : WsEvent> broadcast(
        boardId: Uuid,
        msg: T,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            transaction {
                BoardEventEntity.new {
                    this.board = BoardEntity.Companion[boardId]
                    this.actor = UserEntity.Companion[msg.actor.id]
                    this.event = msg
                }
            }
        }
        connections[boardId]?.forEach { it.send(msg) }
    }

    fun unregister(
        boardId: Uuid,
        ws: Websocket,
    ) = connections[boardId]?.remove(ws) == true

    fun closeAll(boardId: Uuid) {
        connections.remove(boardId)?.forEach { it.close() }
    }
}
