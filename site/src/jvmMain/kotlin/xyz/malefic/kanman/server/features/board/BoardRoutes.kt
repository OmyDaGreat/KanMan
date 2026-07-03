package xyz.malefic.kanman.server.features.board

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import xyz.malefic.kanman.server.infra.http.apiAuth
import xyz.malefic.kanman.server.infra.http.apiAuthOptional
import xyz.malefic.kanman.server.infra.http.apiIdAuth
import xyz.malefic.kanman.server.infra.http.getId
import xyz.malefic.kanman.server.infra.http.model
import xyz.malefic.kanman.server.infra.http.pagination
import xyz.malefic.kanman.server.infra.http.response
import xyz.malefic.kanman.shared.data.model.BoardAction.VIEW_BOARD
import xyz.malefic.kanman.shared.data.model.Column

val boardRoutes =
    arrayOf(
        "/api/boards" bind GET to
            apiAuthOptional { user, request ->
                response(OK, user publicBoardsWith request.pagination())
            },
        "/api/boards" bind POST to
            apiAuth { user, request ->
                response(OK, user create request.model())
            },
        "/api/boards/{id}" bind GET to
            apiIdAuth { user, id, request ->
                val board = getBoard(id, VIEW_BOARD, user)

                request.query("column")?.let {
                    return@apiIdAuth response(
                        OK,
                        board.stickies.filter { sticky -> sticky.column == Column.valueOf(it.trim().uppercase()) },
                    )
                }

                response(OK, board)
            },
        "/api/boards/{id}" bind DELETE to
            apiIdAuth { user, board, _ ->
                user delete board

                response(OK)
            },
        "/api/boards/{id}/history" bind GET to
            apiIdAuth { user, board, request ->
                response(OK, user historyOf board with request.pagination())
            },
        "/api/boards/{id}/users" bind GET to
            apiIdAuth { user, board, _ ->
                response(OK, user getUsers board)
            },
        "/api/boards/{id}/users" bind POST to
            apiIdAuth { user, board, _ ->
                response(OK, user join board)
            },
        "/api/boards/{id}/users/{user_id}" bind DELETE to
            apiIdAuth { user, board, request ->
                response(OK, user kick request.getId("user_id") from board)
            },
        "/api/boards/{id}/users/{user_id}" bind PATCH to
            apiIdAuth { user, board, request ->
                response(OK, user update request.getId("user_id") from board to request.model())
            },
    )
