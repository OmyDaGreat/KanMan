package xyz.malefic.kanman.board

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path
import org.http4k.websocket.WsResponse
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.Boards
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Board.InvalidId
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.util.apiAuth
import xyz.malefic.kanman.util.apiAuthWS
import kotlin.reflect.KProperty1
import kotlin.uuid.Uuid

context(_: Raise<Issue>)
fun Request.boardId(field: String = "id"): Uuid = ensureNotNull(path(field)?.let { Uuid.parseOrNull(it) }) { InvalidId() }

fun apiBoardAuth(
    field: String = "id",
    handler: suspend Raise<Issue>.(UserResponseModel, Uuid, Request) -> Response,
) = apiAuth { user, request -> handler(user, request.boardId(field), request) }

fun apiBoardAuthWS(
    field: String = "id",
    handler: suspend Raise<Issue>.(UserResponseModel, Uuid, Request) -> WsResponse,
) = apiAuthWS { user, request -> handler(user, request.boardId(field), request) }

fun Request.pagination() = (query("page")?.toIntOrNull() ?: 1) to (query("limit")?.toIntOrNull() ?: 50)

context(_: Raise<Issue>, _: JdbcTransaction)
fun fetchBoard(
    id: Uuid,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = ensureNotNull(
    BoardEntity.find { Boards.id eq id }.with(*relations).firstOrNull(),
) { Issue.Board.NotFound() }

context(_: Raise<Issue.Board.AccessDenied>, _: JdbcTransaction)
private fun UserResponseModel?.viewCheck(board: BoardEntity) {
    ensure(board.visibility == Visibility.PUBLIC || board.users.any { it.id.value == this?.id })
    { Issue.Board.AccessDenied("You don't have permission to view this board") }
}

context(_: Raise<Issue>, _: JdbcTransaction)
fun UserResponseModel?.getAccessibleBoard(
    id: Uuid,
    vararg relations: KProperty1<out Entity<*>, Any?>,
) = fetchBoard(id, BoardEntity::users, *relations).also { viewCheck(it) }
