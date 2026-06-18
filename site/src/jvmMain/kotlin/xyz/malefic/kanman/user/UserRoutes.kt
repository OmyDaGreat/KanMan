package xyz.malefic.kanman.user

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.routing.bind
import xyz.malefic.kanman.auth.currentUser
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.util.catch
import xyz.malefic.kanman.util.catchPlus
import xyz.malefic.kanman.util.error
import xyz.malefic.kanman.util.model
import xyz.malefic.kanman.util.response
import xyz.malefic.kanman.util.toVisibility

val userRoutes =
    listOf(
        "/api/boards" bind GET to
            catch("Failed to list boards") { request ->
                val visibility = request.query("visibility")?.toVisibility
                val user = currentUser(request)
                val boards =
                    getUserBoards(visibility, user) ?: return@catch error(UNAUTHORIZED) { "Authentication required for private boards" }

                response(OK, boards)
            },
        "/api/user/register" bind POST to
            catchPlus("Failed to register user") {
                model<UserRequestModel> { _, user ->
                    val userResult = createUser(user)

                    response(OK, userResult)
                }
            },
    )
