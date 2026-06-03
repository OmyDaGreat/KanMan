package xyz.malefic.kanman.util

import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

object ConnectionRegistry {
    private val connections = ConcurrentHashMap<Uuid, MutableSet<Websocket>>()

    fun register(
        boardId: Uuid,
        ws: Websocket,
    ) = connections.getOrPut(boardId) { ConcurrentHashMap.newKeySet() }.add(ws)

    fun broadcast(
        boardId: Uuid,
        msg: WsMessage,
    ) = connections[boardId]?.forEach { it.send(msg) }

    fun unregister(
        boardId: Uuid,
        ws: Websocket,
    ) = connections[boardId]?.remove(ws) == true

    fun closeAll(boardId: Uuid) = connections.remove(boardId)?.forEach { it.close() }
}
