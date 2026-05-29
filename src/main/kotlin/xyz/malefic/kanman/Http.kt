package xyz.malefic.kanman

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.AllowAllOriginPolicy
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

val http: RoutingHttpHandler =
    ServerFilters.Cors(corsPolicy).then(
        routes(
            "/api/ping" bind GET to { Response(OK).body("pong") },
            "/api/health" bind GET to { Response(OK).body("healthy") },
            "/api/user/register" bind POST to POST@{ request ->
                val username =
                    request.query("username") ?: return@POST Response(Status.BAD_REQUEST).body("Expected username, instead got nothing")
                val password =
                    request.query("password") ?: return@POST Response(Status.BAD_REQUEST).body("Expected password, instead got nothing")

                // TODO: More proper user creds validation (+ password hashing)

                try {
                    transaction {
                        UserEntity.new {
                            this.username = username
                            this.password = password
                        }
                    }
                } catch (e: Exception) {
                    return@POST Response(Status.BAD_REQUEST).body("Failed to create user: $e")
                }

                Response(OK).body("User added with username $username and password $password")
            },
            "/api/board/create" bind POST to POST@{ request ->
                val title = request.query("title") ?: return@POST Response(Status.BAD_REQUEST).body("Expected title, instead got nothing")
                val visibilityStr =
                    request.query("visibility")
                        ?: return@POST Response(Status.BAD_REQUEST).body("Expected visibility (PUBLIC/PRIVATE), instead got nothing")
                val visibility =
                    try {
                        Visibility.valueOf(visibilityStr.uppercase())
                    } catch (_: IllegalArgumentException) {
                        return@POST Response(Status.BAD_REQUEST)
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
                    return@POST Response(Status.BAD_REQUEST).body("Failed to create board: $e")
                }

                Response(OK).body("Board created with title $title and visibility $visibility")
            },
        ),
    )
