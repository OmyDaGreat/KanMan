package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.deleteAuth
import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.postAuth
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardEventModel
import xyz.malefic.kanman.data.model.BoardResponseModel
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.PaginatedResponse
import xyz.malefic.kanman.data.model.Role
import xyz.malefic.kanman.data.model.UserSummaryModel
import xyz.malefic.kanman.data.model.Visibility
import kotlin.uuid.Uuid

suspend fun getBoards(
    visibility: Visibility? = null,
    page: Int = 1,
    limit: Int = 50,
) = getAuth<PaginatedResponse<BoardSummaryModel>>(
    "boards?page=$page&limit=$limit${visibility?.let { "&visibility=$it" } ?: ""}",
)

suspend fun getBoard(id: Uuid) = getAuth<BoardResponseModel>("boards/$id")

suspend fun createBoard(board: BoardCreateModel) = postAuth<_, BoardResponseModel>("boards", board)

suspend fun deleteBoard(id: Uuid) = deleteAuth("boards/$id")

suspend fun boardHistory(
    id: Uuid,
    page: Int = 1,
    limit: Int = 50,
) = getAuth<PaginatedResponse<BoardEventModel>>("boards/$id/history?page=$page&limit=$limit")

suspend fun boardUsers(id: Uuid) = getAuth<List<UserSummaryModel>>("boards/$id/users")

suspend fun invite(
    boardId: Uuid,
    userId: Uuid,
    role: Role,
) = postAuth<InviteRequest, List<UserSummaryModel>>("boards/$boardId/users", InviteRequest(userId, role))

suspend fun kick(
    boardId: Uuid,
    userId: Uuid,
) = deleteAuth("boards/$boardId/users/$userId")
