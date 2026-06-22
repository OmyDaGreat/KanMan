package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.get
import xyz.malefic.kanman.data.model.BoardSummaryModel
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.data.model.Visibility

suspend fun boards(
    visibility: Visibility? = null,
    user: UserResponseModel? = null,
) = get<List<BoardSummaryModel>>(if (visibility == Visibility.PUBLIC && user == null) "api/boards/public" else "api/boards")
