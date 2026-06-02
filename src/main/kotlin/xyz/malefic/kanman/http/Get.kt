package xyz.malefic.kanman.http

import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import xyz.malefic.kanman.data.BoardListModel
import xyz.malefic.kanman.data.boardLens
import xyz.malefic.kanman.data.boardListLens
import xyz.malefic.kanman.data.error
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.idLens
import xyz.malefic.kanman.util.auth

val get =
    arrayOf(
        "/api/ping" bind GET to { Response(OK).body("pong") },
        "/api/health" bind GET to { Response(OK).body("healthy") },
        "/api/user/boards" bind GET to
            auth { user ->
                try {
                    Response(OK).with(boardListLens of BoardListModel(user.username, user.boards))
                } catch (e: Exception) {
                    Response(BAD_REQUEST).with(errorLens of "Failed to retrieve boards: $e".error)
                }
            },
        "/api/board" bind GET to
            auth(idLens) { user, id ->
                try {
                    Response(OK).with(
                        boardLens of (
                            user.boards.firstOrNull { it.id == id } ?: return@auth Response(
                                BAD_REQUEST,
                            ).with(errorLens of "Board with id $id not found".error)
                        ),
                    )
                } catch (e: Exception) {
                    Response(BAD_REQUEST).with(errorLens of "Failed to retrieve board with id $id: $e".error)
                }
            },
    )
