package xyz.malefic.kanman.server.features.user

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.server.data.BoardEntity
import xyz.malefic.kanman.server.data.BoardUsers
import xyz.malefic.kanman.server.data.Boards
import xyz.malefic.kanman.server.data.UserEntity
import xyz.malefic.kanman.server.data.Users
import xyz.malefic.kanman.server.data.data
import xyz.malefic.kanman.server.features.auth.verifyAccessToken
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Auth.InvalidToken
import xyz.malefic.kanman.shared.data.model.Issue.User
import xyz.malefic.kanman.shared.data.model.PaginatedResponse
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

context(_: Raise<Issue>)
fun UserResponseModel.getJoinedBoards(
    page: Int = 1,
    limit: Int = 50,
) = data {
    val query =
        (Boards innerJoin BoardUsers)
            .select(Boards.columns)
            .where { BoardUsers.user eq id }
    val total = query.count()
    val items =
        BoardEntity
            .wrapRows(query.offset((page - 1L) * limit).limit(limit))
            .with(BoardEntity::owner, BoardEntity::memberships)
            .map { it.toSummaryModel(id) }

    PaginatedResponse(items, page, limit, total)
}

context(_: Raise<Issue>)
fun getUserSummary(username: String) =
    transaction { ensureNotNull(UserEntity.find { Users.username eq username }.firstOrNull()) { User.NotFound() }.toSummaryModel() }

context(_: Raise<Issue>)
fun getUserSummary(id: Uuid) = transaction { ensureNotNull(UserEntity.findById(id)) { User.NotFound() }.toSummaryModel() }
