package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.deleteAuth
import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.postAuth
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardEventModel
import xyz.malefic.kanman.data.model.BoardResponseModel
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.InviteRequest.Companion.invite
import xyz.malefic.kanman.data.model.PaginatedResponse
import xyz.malefic.kanman.data.model.UserSummaryModel
import xyz.malefic.kanman.data.model.Visibility
import kotlin.uuid.Uuid

suspend fun board(id: Uuid) = getAuth<BoardResponseModel>("board/$id")

suspend fun board(board: BoardCreateModel) = postAuth<_, BoardResponseModel>("board", board)

suspend fun deleteBoard(id: Uuid) = deleteAuth("board/$id")

suspend fun boardHistory(
    id: Uuid,
    page: Int = 1,
    limit: Int = 50,
) = getAuth<PaginatedResponse<BoardEventModel>>("board/$id/history?page=$page&limit=$limit")

suspend fun boardUsers(id: Uuid) = getAuth<List<UserSummaryModel>>("board/$id/users")

suspend fun inviteToBoard(
    boardId: Uuid,
    userId: Uuid,
) = postAuth<InviteRequest, List<UserSummaryModel>>("board/$boardId/users", userId.invite)

suspend fun boards(
    visibility: Visibility? = null,
    page: Int = 1,
    limit: Int = 50,
) = getAuth<PaginatedResponse<BoardSummaryModel>>(
    "boards?page=$page&limit=$limit${visibility?.let { "&visibility=$it" } ?: ""}",
)
