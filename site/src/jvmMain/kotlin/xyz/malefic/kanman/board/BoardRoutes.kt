package xyz.malefic.kanman.board

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.Column
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.Companion.toVisibility
import xyz.malefic.kanman.util.authModel
import xyz.malefic.kanman.util.authRequest
import xyz.malefic.kanman.util.catch
import xyz.malefic.kanman.util.catchPlus
import xyz.malefic.kanman.util.error
import xyz.malefic.kanman.util.response
import kotlin.uuid.Uuid

val boardRoutes =
    arrayOf(
        "/api/board/{id}" bind GET to
            catchPlus("Failed to retrieve board") {
                authRequest { user ->
                    val id = path("id")?.let { Uuid.parse(it) } ?: return@authRequest error(BAD_REQUEST) { "Invalid board id" }
                    val board = user.boards.firstOrNull { it.id == id } ?: return@authRequest error(NOT_FOUND) { "Board not found" }
                    query("column")?.let {
                        return@authRequest response(
                            OK,
                            board.stickies.filter { sticky ->
                                sticky.column ==
                                    Column.valueOf(it.trim().uppercase())
                            },
                        )
                    }

                    response(OK, board)
                }
            },
        "/api/board" bind POST to
            catchPlus("Failed to create board") {
                authModel<BoardCreateModel> { user, boardRequest ->
                    val boardResponse = createBoard(boardRequest, user)

                    response(OK, boardResponse)
                }
            },
        "/api/board/{id}" bind DELETE to
            authRequest { user ->
                val id =
                    Uuid.parseOrNull(path("id") ?: return@authRequest error(BAD_REQUEST) { "Invalid board" })
                        ?: return@authRequest error(BAD_REQUEST) { "Invalid board" }

                try {
                    if (!deleteBoard(id, user)) {
                        return@authRequest error(BAD_REQUEST) { "Invalid board" }
                    }
                } catch (e: Exception) {
                    return@authRequest error(INTERNAL_SERVER_ERROR) { "Failed to delete board: $e" }
                }

                response(OK)
            },
        "/api/boards/public" bind GET to
            catch("Failed to list public boards") {
                val boards = getBoards(Visibility.PUBLIC, null)!!

                response(OK, boards)
            },
        "/api/boards" bind GET to
            catchPlus("Failed to list boards") {
                authRequest { user ->
                    val visibility = query("visibility")?.toVisibility
                    val boards =
                        getBoards(visibility, user)
                            ?: return@authRequest error(UNAUTHORIZED) { "Authentication required for private boards" }

                    response(OK, boards)
                }
            },
    )
