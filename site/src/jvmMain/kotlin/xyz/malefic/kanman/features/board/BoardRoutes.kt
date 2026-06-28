package xyz.malefic.kanman.features.board

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.Column
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.Visibility.Companion.toVisibility
import xyz.malefic.kanman.infra.http.apiAuth
import xyz.malefic.kanman.infra.http.apiAuthOptional
import xyz.malefic.kanman.infra.http.apiBoardAuth
import xyz.malefic.kanman.infra.http.model
import xyz.malefic.kanman.infra.http.pagination
import xyz.malefic.kanman.infra.http.response

val boardRoutes =
    arrayOf(
        "/api/boards" bind GET to
            apiAuthOptional { user, request ->
                val visibility = request.query("visibility")?.toVisibility
                val (page, limit) = request.pagination()

                response(OK, getBoards(user, visibility, page, limit))
            },
        "/api/boards/{id}" bind GET to
            apiBoardAuth { user, id, request ->
                val board = getBoard(id, user)

                request.query("column")?.let {
                    return@apiBoardAuth response(
                        OK,
                        board.stickies.filter { sticky -> sticky.column == Column.valueOf(it.trim().uppercase()) },
                    )
                }

                response(OK, board)
            },
        "/api/boards" bind POST to
            apiAuth { user, request ->
                val boardRequest = request.model<BoardCreateModel>()
                val boardResponse = createBoard(boardRequest, user)

                response(OK, boardResponse)
            },
        "/api/boards/{id}" bind DELETE to
            apiBoardAuth { user, id, _ ->
                user.deleteBoard(id)

                response(OK)
            },
        "/api/boards/{id}/history" bind GET to
            apiBoardAuth { user, id, request ->
                val (page, limit) = request.pagination()

                response(OK, user.getBoardHistory(id, page, limit))
            },
        "/api/boards/{id}/users" bind GET to
            apiBoardAuth { user, id, _ ->
                response(OK, user.getBoardUsers(id))
            },
        "/api/boards/{id}/users" bind POST to
            apiBoardAuth { user, id, request ->
                val addUser = request.model<InviteRequest>()

                response(OK, user.inviteToBoard(id, addUser))
            },
    )
