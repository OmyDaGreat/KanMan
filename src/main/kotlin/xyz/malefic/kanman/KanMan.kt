package xyz.malefic.kanman

import org.http4k.routing.poly
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
