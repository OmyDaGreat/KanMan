package xyz.malefic.kanman.util

import org.http4k.websocket.Websocket
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
    ) = connections[boardId]?.forEach { it.send(msg) }

    fun unregister(
        boardId: Uuid,
        ws: Websocket,
    ) = connections[boardId]?.remove(ws) == true

    fun closeAll(boardId: Uuid) = connections.remove(boardId)?.forEach { it.close() }
}
