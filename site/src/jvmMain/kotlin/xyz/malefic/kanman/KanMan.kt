package xyz.malefic.kanman

import org.http4k.core.Method.GET
import org.http4k.filter.debug
import org.http4k.routing.bind
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.AuthTokens
import xyz.malefic.kanman.data.BoardUsers
import xyz.malefic.kanman.data.Boards
import xyz.malefic.kanman.data.SQLKermit
import xyz.malefic.kanman.data.StickyNotes
import xyz.malefic.kanman.data.Users
import xyz.malefic.kanman.http.http
import xyz.malefic.kanman.util.serveStaticFile

fun main() {
    Database.connect(
        url = "jdbc:sqlite:data.db?foreign_keys=on", // TODO: Replace "data.db"
        driver = "org.sqlite.JDBC",
    )

    transaction {
        addLogger(SQLKermit)
        SchemaUtils.create(Users, AuthTokens, Boards, StickyNotes, BoardUsers)
    }

    val server =
        poly(
            routes(
                "/api" bind http,
                "/{path:.*}" bind GET to ::serveStaticFile,
            ),
            ws,
        ).debug().asServer(Undertow(6320)).start()

    println("Server started on port ${server.port()}!")
}
