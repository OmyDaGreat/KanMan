package xyz.malefic.kanman.server.data

import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.shared.data.model.UserResponseModel

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

fun <A : CompositeEntityClass<B>, B : CompositeEntity> A.findById(id: (CompositeID) -> Unit) = findById(CompositeID.Companion(id))

fun <R : UserResponseModel?, T> R.data(block: context(JdbcTransaction) R.() -> T): T = transaction { this@data.block() }

context(_: JdbcTransaction)
val UserResponseModel.entity
    get() = UserEntity.findById(id) ?: throw IllegalArgumentException("User with ID $id not found")
