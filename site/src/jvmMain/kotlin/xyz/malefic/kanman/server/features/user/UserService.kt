package xyz.malefic.kanman.server.features.user

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.server.data.AssignedUserEntity
import xyz.malefic.kanman.server.data.AssignedUsers
import xyz.malefic.kanman.server.data.BoardEntity
import xyz.malefic.kanman.server.data.BoardEventEntity
import xyz.malefic.kanman.server.data.BoardEvents
import xyz.malefic.kanman.server.data.BoardUsers
import xyz.malefic.kanman.server.data.Boards
import xyz.malefic.kanman.server.data.StickyNoteEntity
import xyz.malefic.kanman.server.data.StickyNotes
import xyz.malefic.kanman.server.data.UserEntity
import xyz.malefic.kanman.server.data.Users
import xyz.malefic.kanman.server.data.data
import xyz.malefic.kanman.server.features.auth.verifyAccessToken
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Auth.InvalidToken
import xyz.malefic.kanman.shared.data.model.Issue.User
import xyz.malefic.kanman.shared.data.model.PaginatedResponse
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.UserUpdateModel
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
infix fun UserResponseModel.patch(updates: UserUpdateModel) =
    data {
        val entity = ensureNotNull(UserEntity.findById(id)) { User.NotFound() }

        updates.username?.let { newUsername ->
            if (newUsername != entity.username) {
                ensure(UserEntity.find { Users.username eq newUsername }.empty()) { User.AlreadyExists() }
                entity.username = newUsername
            }
        }

        updates.profilePicture?.let { entity.profilePicture = it }

        entity.toResponseModel()
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
fun UserResponseModel.getAssignedTasks() =
    data {
        val query =
            (StickyNotes innerJoin AssignedUsers)
                .select(StickyNotes.columns)
                .where { AssignedUsers.user eq id }
                .orderBy(AssignedUsers.due to SortOrder.ASC_NULLS_LAST)

        StickyNoteEntity
            .wrapRows(query)
            .with(StickyNoteEntity::board, StickyNoteEntity::assignedUsers, AssignedUserEntity::user)
            .map { it.toModel() }
    }

context(_: Raise<Issue>)
fun UserResponseModel.getGlobalHistory(
    page: Int = 1,
    limit: Int = 50,
) = data {
    val joinedBoardIds =
        BoardUsers
            .select(BoardUsers.board)
            .where { BoardUsers.user eq id }
            .map { it[BoardUsers.board] }

    val query =
        BoardEvents
            .select(BoardEvents.columns)
            .where { BoardEvents.board inList joinedBoardIds }
            .orderBy(BoardEvents.timestamp to SortOrder.DESC)

    val total = query.count()
    val items =
        BoardEventEntity
            .wrapRows(query.offset((page - 1L) * limit).limit(limit))
            .with(BoardEventEntity::board, BoardEventEntity::actor)
            .map { it.toModel() }

    PaginatedResponse(items, page, limit, total)
}

context(_: Raise<Issue>)
fun getUserSummary(username: String) =
    transaction { ensureNotNull(UserEntity.find { Users.username eq username }.firstOrNull()) { User.NotFound() }.toSummaryModel() }

context(_: Raise<Issue>)
fun getUserSummary(id: Uuid) = transaction { ensureNotNull(UserEntity.findById(id)) { User.NotFound() }.toSummaryModel() }
