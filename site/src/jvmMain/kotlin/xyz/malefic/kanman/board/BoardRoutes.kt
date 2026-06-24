package xyz.malefic.kanman.board

import arrow.core.raise.ensureNotNull
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.Column
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.Issue.Board.InvalidId
import xyz.malefic.kanman.data.model.Visibility.Companion.toVisibility
import xyz.malefic.kanman.data.model.Visibility.PUBLIC
import xyz.malefic.kanman.util.api
import xyz.malefic.kanman.util.apiAuth
import xyz.malefic.kanman.util.model
import xyz.malefic.kanman.util.response
import kotlin.uuid.Uuid

val boardRoutes =
    arrayOf(
        "/api/board/{id}" bind GET to
            apiAuth { user, request ->
                val id = ensureNotNull(request.path("id")?.let { Uuid.parseOrNull(it) }) { InvalidId() }
                val board = user.boards.firstOrNull { it.id == id } ?: getBoard(id, user)

                request.query("column")?.let {
                    return@apiAuth response(
                        OK,
                        board.stickies.filter { sticky -> sticky.column == Column.valueOf(it.trim().uppercase()) },
                    )
                }

                response(OK, board)
            },
        "/api/board" bind POST to
            apiAuth { user, request ->
                val boardRequest = request.model<BoardCreateModel>()
                val boardResponse = createBoard(boardRequest, user)

                response(OK, boardResponse)
            },
        "/api/board/{id}" bind DELETE to
            apiAuth { user, request ->
                val id = ensureNotNull(request.path("id")?.let { Uuid.parseOrNull(it) }) { InvalidId() }

                deleteBoard(id, user)

                response(OK)
            },
        "/api/board/{id}/users" bind GET to
            apiAuth { user, request ->
                val id = ensureNotNull(request.path("id")?.let { Uuid.parseOrNull(it) }) { InvalidId() }

                response(OK, getBoardUsers(id, user))
            },
        "/api/board/{id}/users" bind POST to
            apiAuth { user, request ->
                val boardId = ensureNotNull(request.path("id")?.let { Uuid.parseOrNull(it) }) { InvalidId() }
                val addUser = request.model<InviteRequest>()

                response(OK, user.inviteToBoard(boardId, addUser))
            },
        "/api/boards/public" bind GET to
            api { _ ->
                val boards = getBoards(visibility = PUBLIC)

                response(OK, boards)
            },
        "/api/boards" bind GET to
            apiAuth { user, request ->
                val visibility = request.query("visibility")?.toVisibility
                val boards = getBoards(user, visibility)

                response(OK, boards)
            },
    )
