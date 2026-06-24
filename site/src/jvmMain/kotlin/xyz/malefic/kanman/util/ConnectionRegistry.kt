package xyz.malefic.kanman.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.http4k.websocket.Websocket
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.BoardEventEntity
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.model.WsEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

object ConnectionRegistry {
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
                    this.board = BoardEntity[boardId]
                    this.actor = UserEntity[msg.actor.id]
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

    fun closeAll(boardId: Uuid) = connections.remove(boardId)?.forEach { it.close() }
}
