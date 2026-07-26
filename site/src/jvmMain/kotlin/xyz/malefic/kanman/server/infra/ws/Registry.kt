package xyz.malefic.kanman.server.infra.ws

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.server.data.BoardEntity
import xyz.malefic.kanman.server.data.BoardEventEntity
import xyz.malefic.kanman.server.data.UserEntity
import xyz.malefic.kanman.shared.api.util.json
import xyz.malefic.kanman.shared.data.model.WsEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

object Registry {
    val connections = ConcurrentHashMap<Uuid, MutableSet<Websocket>>()

    fun register(
        boardId: Uuid,
        ws: Websocket,
    ) = connections.getOrPut(boardId) { ConcurrentHashMap.newKeySet() }.add(ws)

    fun broadcast(
        boardId: Uuid,
        msg: WsEvent,
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
        val encoded = json.encodeToString(WsEvent.serializer(), msg)
        connections[boardId]?.forEach { it.send(WsMessage(encoded)) }
    }

    fun unregister(
        boardId: Uuid,
        ws: Websocket,
    ) = connections[boardId]?.remove(ws) == true

    fun closeAll(boardId: Uuid) {
        connections.remove(boardId)?.forEach { it.close() }
    }
}
