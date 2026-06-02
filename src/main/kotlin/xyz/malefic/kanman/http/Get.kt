package xyz.malefic.kanman.http

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import xyz.malefic.kanman.data.BoardListModel
import xyz.malefic.kanman.data.boardListLens
import xyz.malefic.kanman.data.error
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.util.auth

val get =
    arrayOf(
        "/api/ping" bind Method.GET to { Response(OK).body("pong") },
        "/api/health" bind Method.GET to { Response(OK).body("healthy") },
        "/api/user/boards" bind Method.GET to
            auth { user ->
                try {
                    Response(OK).with(boardListLens of BoardListModel(user.username, user.boards))
                } catch (e: Exception) {
                    Response(BAD_REQUEST).with(errorLens of "Failed to retrieve boards: $e".error)
                }
            },
    )
