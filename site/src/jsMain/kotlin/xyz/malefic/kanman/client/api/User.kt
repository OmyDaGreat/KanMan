package xyz.malefic.kanman.client.api

import xyz.malefic.kanman.client.api.util.getAuth
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel
import xyz.malefic.kanman.shared.data.model.PaginatedResponse
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.UserSummaryModel

suspend fun getUser() = getAuth<UserResponseModel>("me")

suspend fun getJoinedBoards(
    page: Int = 1,
    limit: Int = 50,
) = getAuth<PaginatedResponse<BoardSummaryModel>>("boards?page=$page&limit=$limit}")

suspend fun getUser(username: String) = getAuth<UserSummaryModel>("users/$username")
