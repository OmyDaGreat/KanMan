package xyz.malefic.kanman.http

import org.http4k.core.Method.DELETE
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.data.transaction.deleteBoard
import xyz.malefic.kanman.util.auth
import xyz.malefic.kanman.util.error
import kotlin.uuid.Uuid

val delete =
    arrayOf(
        "/api/board/{id}" bind DELETE to
            auth REQUEST@{ user, request ->
                val id = Uuid.parse(request.path("id") ?: return@REQUEST Response(BAD_REQUEST).with("Invalid board id".error))

                try {
                    deleteBoard(id, user)?.let { return@REQUEST it }
                } catch (e: Exception) {
                    return@REQUEST Response(INTERNAL_SERVER_ERROR).with("Failed to create board: $e".error)
                }

                Response(OK)
            },
    )
