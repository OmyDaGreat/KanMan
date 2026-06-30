package xyz.malefic.kanman.server.features.invitation

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.server.infra.http.apiAuth
import xyz.malefic.kanman.server.infra.http.response
import kotlin.uuid.Uuid

val invitationRoutes =
    arrayOf(
        "/api/invitations" bind GET to
            apiAuth { user, _ ->
                response(OK, user.getInvites())
            },
        "/api/invitations/{id}/accept" bind POST to
            apiAuth { user, request ->
                val inviteId = Uuid.parse(request.path("id")!!)
                response(OK, user.acceptInvite(inviteId))
            },
        "/api/invitations/{id}" bind DELETE to
            apiAuth { user, request ->
                val inviteId = Uuid.parse(request.path("id")!!)
                user.declineInvite(inviteId)
                response(OK)
            },
    )
