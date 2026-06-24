package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.deleteAuth
import xyz.malefic.kanman.api.util.get
import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.postAuth
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardModel
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.InviteRequest
import xyz.malefic.kanman.data.model.InviteRequest.Companion.invite
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PUBLIC
import kotlin.uuid.Uuid

suspend fun board(id: Uuid) = getAuth<BoardModel>("board/$id")

suspend fun board(board: BoardCreateModel) = postAuth<_, BoardModel>("board", board)

suspend fun deleteBoard(id: Uuid) = deleteAuth("board/$id")

suspend fun boardUsers(id: Uuid) = getAuth<List<UserResponseModel>>("board/$id/users")

suspend fun inviteToBoard(
    boardId: Uuid,
    userId: Uuid,
) = postAuth<InviteRequest, List<UserResponseModel>>("board/$boardId/users", userId.invite)

suspend fun boards(
    visibility: Visibility? = null,
    user: UserResponseModel? = null,
) = get<List<BoardSummaryModel>>(
    if (visibility == PUBLIC && user == null) {
        "boards/public"
    } else {
        "boards${if (user != null) "?user=${user.id}" else ""}${if (visibility != null) "${if (user != null) "&" else "?"}visibility=${visibility.name}" else ""}"
    },
)
