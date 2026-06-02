package xyz.malefic.kanman.util

import org.http4k.websocket.Websocket
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

object ConnectionRegistry {
    private val connections = ConcurrentHashMap<Uuid, MutableSet<Websocket>>()

    fun register(
        boardId: Uuid,
        ws: Websocket,
    ) = connections.getOrPut(boardId) { ConcurrentHashMap.newKeySet() }.add(ws)

    fun unregister(
        boardId: Uuid,
        ws: Websocket,
    ) = connections[boardId]?.remove(ws)

    fun closeAll(boardId: Uuid) = connections.remove(boardId)?.forEach { it.close() }
}
