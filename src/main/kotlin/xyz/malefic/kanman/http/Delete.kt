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
import xyz.malefic.kanman.util.authRequest
import xyz.malefic.kanman.util.error
import kotlin.uuid.Uuid

val delete =
    arrayOf(
        "/api/board/{id}" bind DELETE to
            authRequest { user ->
                val id =
                    Uuid.parseOrNull(path("id") ?: return@authRequest Response(BAD_REQUEST).with("Invalid board".error))
                        ?: return@authRequest Response(BAD_REQUEST).with("Invalid board id".error)

                try {
                    if (!deleteBoard(id, user)) {
                        return@authRequest Response(BAD_REQUEST).with("Invalid board".error)
                    }
                } catch (e: Exception) {
                    return@authRequest Response(INTERNAL_SERVER_ERROR).with("Failed to create board: $e".error)
                }

                Response(OK)
            },
    )
