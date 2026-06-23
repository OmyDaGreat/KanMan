package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.deleteAuth
import xyz.malefic.kanman.api.util.get
import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.postAuth
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardModel
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility
import xyz.malefic.kanman.data.model.Visibility.PUBLIC
import kotlin.uuid.Uuid

suspend fun board(id: Uuid) = getAuth<BoardModel>("api/board/$id")

suspend fun board(board: BoardCreateModel) = postAuth<BoardCreateModel, BoardModel>("api/board", board)

suspend fun deleteBoard(id: Uuid) = deleteAuth("api/board/$id")

suspend fun inviteToBoard(
    boardId: Uuid,
    userId: Uuid,
) = postAuth<Unit, List<UserResponseModel>>("api/board/$boardId/invite/$userId", Unit)

suspend fun boards(
    visibility: Visibility? = null,
    user: UserResponseModel? = null,
) = get<List<BoardSummaryModel>>(
    if (visibility == PUBLIC && user == null) {
        "api/boards/public"
    } else {
        "api/boards${if (user != null) "?user=${user.id}" else ""}${if (visibility != null) "${if (user != null) "&" else "?"}visibility=${visibility.name}" else ""}"
    },
)
