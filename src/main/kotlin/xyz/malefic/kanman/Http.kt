package xyz.malefic.kanman

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.BoardEntity
import xyz.malefic.kanman.data.UserEntity
import xyz.malefic.kanman.data.Users
import xyz.malefic.kanman.data.Visibility
import kotlin.uuid.ExperimentalUuidApi

@ExperimentalUuidApi
val http: RoutingHttpHandler =
    ServerFilters.Cors(corsPolicy).then(
        routes(
            "/api/ping" bind GET to { Response(OK).body("pong") },
            "/api/health" bind GET to { Response(OK).body("healthy") },
            "/api/user/register" bind POST to REQUEST@{ request ->
                val username =
                    request.query("username") ?: return@REQUEST Response(BAD_REQUEST).body("Expected username, instead got nothing")
                val password =
                    request.query("password") ?: return@REQUEST Response(BAD_REQUEST).body("Expected password, instead got nothing")

                // TODO: More proper user creds validation (+ password hashing)

                try {
                    transaction {
                        UserEntity.new {
                            this.username = username
                            this.password = password
                        }
                    }
                } catch (e: Exception) {
                    return@REQUEST Response(BAD_REQUEST).body("Failed to create user: $e")
                }

                Response(OK).body("User added with username $username and password $password")
            },
            "/api/user/boards" bind GET to REQUEST@{ request ->
                val username =
                    request.query("username") ?: return@REQUEST Response(BAD_REQUEST).body("Expected username, instead got nothing")
                val password =
                    request.query("password") ?: return@REQUEST Response(BAD_REQUEST).body("Expected password, instead got nothing")

                try {
                    transaction {
                        UserEntity.find { Users.username eq username }.firstOrNull()?.let { user ->
                            if (user.password != password) {
                                return@transaction Response(UNAUTHORIZED).body("Invalid password")
                            }
                            val boards =
                                user.boards.joinToString(",\n") { board ->
                                    " - ${board.id}: \"${board.title}\" (${board.visibility})"
                                }
                            Response(OK).body("Boards for user $username:\n$boards")
                        } ?: Response(NOT_FOUND).body("User not found")
                    }
                } catch (e: Exception) {
                    return@REQUEST Response(BAD_REQUEST).body("Failed to retrieve boards: $e")
                }
            },
            "/api/board/create" bind POST to REQUEST@{ request ->
                val title =
                    request.query("title") ?: return@REQUEST Response(BAD_REQUEST).body("Expected title, instead got nothing")
                val visibilityStr =
                    request.query("visibility")
                        ?: return@REQUEST Response(BAD_REQUEST).body("Expected visibility (PUBLIC/PRIVATE), instead got nothing")
                val visibility =
                    try {
                        Visibility.valueOf(visibilityStr.uppercase())
                    } catch (_: IllegalArgumentException) {
                        return@REQUEST Response(BAD_REQUEST)
                            .body("Invalid visibility '$visibilityStr'. Expected PUBLIC or PRIVATE.")
                    }

                try {
                    transaction {
                        BoardEntity.new {
                            this.title = title
                            this.visibility = visibility
                        }
                    }
                } catch (e: Exception) {
                    return@REQUEST Response(BAD_REQUEST).body("Failed to create board: $e")
                }

                Response(OK).body("Board created with title $title and visibility $visibility")
            },
        ),
    )
