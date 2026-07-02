package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.PaginatedResponse

@Composable
fun InfiniteBoardView(
    title: String,
    request: suspend (Int, Int) -> Either<Issue, PaginatedResponse<BoardSummaryModel>>,
    onBoardClick: (BoardSummaryModel) -> Unit,
) {
    var page by remember { mutableStateOf(1) }
    var allBoards by remember { mutableStateOf(listOf<BoardSummaryModel>()) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 10

    LaunchedEffect(page) {
        request(page, limit).fold(
            { /* handle error */ },
            { response ->
                allBoards = allBoards + response.items
                hasMore = allBoards.size < response.totalItems
            },
        )
    }

    BoardShowcase(title, allBoards, onBoardClick, hasMore to { page++ })
}
