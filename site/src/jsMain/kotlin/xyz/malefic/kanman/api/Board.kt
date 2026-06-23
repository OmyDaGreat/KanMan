package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.deleteAuth
import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.postAuth
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardModel
import kotlin.uuid.Uuid

suspend fun board(id: Uuid) = getAuth<BoardModel>("api/board/$id")

suspend fun board(board: BoardCreateModel) = postAuth<BoardCreateModel, BoardModel>("api/board", board)

suspend fun deleteBoard(id: Uuid) = deleteAuth("api/board/$id")

suspend fun inviteToBoard(
    boardId: Uuid,
    userId: Uuid,
) = postAuth<Unit, List<BoardModel>>("api/board/$boardId/invite/$userId", Unit)
