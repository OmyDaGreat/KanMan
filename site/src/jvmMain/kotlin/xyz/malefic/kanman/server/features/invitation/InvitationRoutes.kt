package xyz.malefic.kanman.server.features.invitation

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import xyz.malefic.kanman.server.infra.http.apiAuth
import xyz.malefic.kanman.server.infra.http.apiIdAuth
import xyz.malefic.kanman.server.infra.http.model
import xyz.malefic.kanman.server.infra.http.response

val invitationRoutes =
    arrayOf(
        "/api/invitations" bind GET to
            apiAuth { user, _ ->
                response(OK, user.getInvites())
            },
        "/api/invitations" bind POST to
            apiAuth { user, request ->
                response(OK, user invite request.model())
            },
        "/api/invitations/{id}/accept" bind POST to
            apiIdAuth { user, id, _ ->
                response(OK, user accept id)
            },
        "/api/invitations/{id}" bind DELETE to
            apiIdAuth { user, id, _ ->
                user decline id
                response(OK)
            },
    )
