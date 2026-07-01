package xyz.malefic.kanman.server.features.board

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.server.features.invitation.invite
import xyz.malefic.kanman.server.infra.http.apiAuth
import xyz.malefic.kanman.server.infra.http.apiAuthOptional
import xyz.malefic.kanman.server.infra.http.apiBoardAuth
import xyz.malefic.kanman.server.infra.http.model
import xyz.malefic.kanman.server.infra.http.pagination
import xyz.malefic.kanman.server.infra.http.response
import xyz.malefic.kanman.shared.data.model.BoardCreateModel
import xyz.malefic.kanman.shared.data.model.Column
import xyz.malefic.kanman.shared.data.model.InviteRequest
import xyz.malefic.kanman.shared.data.model.RoleUpdateRequest
import kotlin.uuid.Uuid

val boardRoutes =
    arrayOf(
        "/api/boards" bind GET to
            apiAuthOptional { user, request ->
                val (page, limit) = request.pagination()

                response(OK, getBoards(user, page, limit))
            },
        "/api/boards/{id}" bind GET to
            apiBoardAuth { user, id, request ->
                val board = getAccessibleBoard(id, user = user)

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
                val boardResponse = user.createBoard(boardRequest)

                response(OK, boardResponse)
            },
        "/api/boards/{id}" bind DELETE to
            apiBoardAuth { user, id, _ ->
                user.deleteBoard(id)

                response(OK)
            },
        "/api/boards/{id}/join" bind POST to
            apiBoardAuth { user, id, _ ->
                response(OK, user.join(id))
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
                response(OK, user.invite(id, request.model<InviteRequest>()).toModel())
            },
        "/api/boards/{id}/users/{userId}" bind DELETE to
            apiBoardAuth { user, id, request ->
                val targetId = Uuid.parse(request.path("userId")!!)
                response(OK, user.kick(id, targetId))
            },
        "/api/boards/{id}/users/{userId}" bind PATCH to
            apiBoardAuth { user, id, request ->
                val targetId = Uuid.parse(request.path("userId")!!)
                response(OK, user.updateUserRole(id, targetId, request.model<RoleUpdateRequest>().role))
            },
    )
