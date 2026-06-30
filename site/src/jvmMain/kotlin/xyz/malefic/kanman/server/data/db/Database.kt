package xyz.malefic.kanman.server.data.db

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun initDatabase() {
    Database.connect(
        url = "jdbc:sqlite:kanban.db?foreign_keys=on",
        driver = "org.sqlite.JDBC",
    )

    transaction {
        addLogger(SQLKermit)
        SchemaUtils.create(Users, AuthTokens, Boards, BoardEvents, StickyNotes, AssignedUsers, BoardUsers)
    }
}
