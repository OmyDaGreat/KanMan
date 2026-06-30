package xyz.malefic.kanman.server.features.user

import arrow.core.raise.ensureNotNull
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import xyz.malefic.kanman.server.infra.http.api
import xyz.malefic.kanman.server.infra.http.response
import xyz.malefic.kanman.shared.data.model.Issue.Validation.BadRequest

val userRoutes =
    arrayOf(
        "/api/users/{username}" bind GET to
            api { request ->
                response(OK, getUserSummary(ensureNotNull(request.path("username")) { BadRequest("Missing username") }))
            },
    )
