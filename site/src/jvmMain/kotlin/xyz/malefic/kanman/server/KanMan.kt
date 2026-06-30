package xyz.malefic.kanman.server

import co.touchlab.kermit.Logger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.filter.debug
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import xyz.malefic.kanman.server.data.initDatabase
import xyz.malefic.kanman.server.features.auth.authRoutes
import xyz.malefic.kanman.server.features.auth.janitor
import xyz.malefic.kanman.server.features.board.boardRoutes
import xyz.malefic.kanman.server.features.board.boardWs
import xyz.malefic.kanman.server.features.invitation.invitationRoutes
import xyz.malefic.kanman.server.features.user.userRoutes
import xyz.malefic.kanman.server.infra.http.rateLimit
import xyz.malefic.kanman.server.infra.http.response
import xyz.malefic.kanman.server.infra.http.serveStaticFile
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun main() {
    initDatabase()

    val throttler = rateLimit(10, 1.minutes.inWholeMilliseconds)

    val http: RoutingHttpHandler =
        ServerFilters.Cors(corsPolicy).then(
            routes(
                "/api/ping" bind GET to { response(OK).body("pong") },
                "/api/health" bind GET to { response(OK).body("healthy") },
                *authRoutes.map { throttler.then(it) }.toTypedArray(),
                *userRoutes,
                *boardRoutes,
                *invitationRoutes,
                "/{path:.*}" bind GET to ::serveStaticFile,
            ),
        )

    val server = poly(http, boardWs).debug().asServer(Undertow(6320)).start()
    Logger.d("Server started on port ${server.port()}!")

    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch {
        val log = Logger.withTag("Janitor")
        while (true) {
            try {
                janitor()
                log.d { "Cleanup successful" }
            } catch (e: Exception) {
                log.e(e) { "Cleanup failed, retrying in 1 hour" }
            }
            delay(1.hours)
        }
    }
}
