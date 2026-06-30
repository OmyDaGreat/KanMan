package xyz.malefic.kanman.server.features.user

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.server.data.db.BoardEntity
import xyz.malefic.kanman.server.data.db.UserEntity
import xyz.malefic.kanman.server.data.db.Users
import xyz.malefic.kanman.server.features.auth.verifyAccessToken
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Auth.InvalidToken
import xyz.malefic.kanman.shared.data.model.Issue.User
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import kotlin.uuid.Uuid

context(_: Raise<Issue>)
fun getUserFromAccessToken(accessToken: String) =
    transaction {
        ensureNotNull(
            UserEntity
                .find { Users.id eq verifyAccessToken(accessToken) }
                .with(UserEntity::boards, BoardEntity::owner)
                .firstOrNull(),
        ) { InvalidToken() }.toResponseModel()
    }

context(_: JdbcTransaction)
val UserResponseModel.entity
    get() = UserEntity.findById(id) ?: throw IllegalArgumentException("User with ID $id not found")

context(_: Raise<Issue>)
fun getUserSummary(username: String) =
    transaction { ensureNotNull(UserEntity.find { Users.username eq username }.firstOrNull()) { User.NotFound() }.toSummaryModel() }

context(_: Raise<Issue>)
fun getUserSummary(id: Uuid) = transaction { ensureNotNull(UserEntity.findById(id)) { User.NotFound() }.toSummaryModel() }
