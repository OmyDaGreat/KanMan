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
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun main() {
    Database.connect(
        url = "jdbc:sqlite:data.db", // TODO: Actually decide file name
        driver = "org.sqlite.JDBC",
    )

    transaction {
        addLogger(SQLKermit)
        exec("PRAGMA foreign_keys = ON;")
        SchemaUtils.create(Users, Boards, StickyNotes, BoardUsers)
    }

    val server = poly(http, ws).asServer(Undertow(6320)).start()

    println("Server started on port ${server.port()}!")
}
