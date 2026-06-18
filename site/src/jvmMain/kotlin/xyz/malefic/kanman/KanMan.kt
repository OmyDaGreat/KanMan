package xyz.malefic.kanman

import org.http4k.core.Method.GET
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.filter.debug
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import xyz.malefic.kanman.auth.authRoutes
import xyz.malefic.kanman.board.boardRoutes
import xyz.malefic.kanman.board.boardWs
import xyz.malefic.kanman.data.db.initDatabase
import xyz.malefic.kanman.user.userRoutes
import xyz.malefic.kanman.util.serveStaticFile

fun main() {
    initDatabase()

    val http: RoutingHttpHandler =
        ServerFilters.Cors(corsPolicy).then(
            routes(
                *(authRoutes + boardRoutes + userRoutes).toTypedArray(),
                "/{path:.*}" bind GET to ::serveStaticFile,
            ),
        )

    val server = poly(http, boardWs).debug().asServer(Undertow(6320)).start()

    println("Server started on port ${server.port()}!")
}
