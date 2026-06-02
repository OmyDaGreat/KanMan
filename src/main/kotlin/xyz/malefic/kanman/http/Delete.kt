package xyz.malefic.kanman.http

import org.http4k.core.Method.DELETE
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.path
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.util.ConnectionRegistry
import xyz.malefic.kanman.util.auth
import xyz.malefic.kanman.util.error
import kotlin.uuid.Uuid

val delete =
    arrayOf(
        "/api/board/{id}" bind DELETE to
            auth REQUEST@{ user, request ->
                val id = Uuid.parse(request.path("id") ?: return@REQUEST Response(BAD_REQUEST).with("Invalid board id".error))

                try {
                    transaction {
                        val board =
                            BoardEntity.findById(id) ?: return@transaction Response(NOT_FOUND).with("Board not found".error)
                        if (board.users.none { u -> u.id.value == user.id }) {
                            return@transaction Response(FORBIDDEN).with("User is not added to board".error)
                        }
                        board.delete()
                        null
                    }?.let { return@REQUEST it }
                } catch (e: Exception) {
                    return@REQUEST Response(INTERNAL_SERVER_ERROR).with("Failed to create board: $e".error)
                }

                ConnectionRegistry.closeAll(id)
                Response(OK)
            },
    )
