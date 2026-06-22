package xyz.malefic.kanman.user

import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.routing.bind
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.util.authRequest
import xyz.malefic.kanman.util.catch
import xyz.malefic.kanman.util.catchPlus
import xyz.malefic.kanman.util.error
import xyz.malefic.kanman.util.response
import xyz.malefic.kanman.util.toVisibility

val userRoutes =
    arrayOf(
        "/api/boards/public" bind GET to
            catch("Failed to list public boards") {
                val boards = getUserBoards(Visibility.PUBLIC, null)!!

                response(OK, boards)
            },
        "/api/boards" bind GET to
            catchPlus("Failed to list boards") {
                authRequest { user ->
                    val visibility = query("visibility")?.toVisibility
                    val boards =
                        getUserBoards(visibility, user)
                            ?: return@authRequest error(UNAUTHORIZED) { "Authentication required for private boards" }

                    response(OK, boards)
                }
            },
    )
