package xyz.malefic.kanman.client.pages.boards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import kotlinx.coroutines.launch
import xyz.malefic.kanman.client.api.getPublicBoards
import xyz.malefic.kanman.client.api.joinBoard
import xyz.malefic.kanman.client.components.PaginatedBoardView
import xyz.malefic.kanman.client.components.handle

@Page
@Composable
fun Public(ctx: PageContext) =
    with(ctx) {
        val scope = rememberCoroutineScope()

        PaginatedBoardView("Public Boards", { page, limit -> getPublicBoards(page, limit) }) { board ->
            scope.launch { handle(joinBoard(board.id)) { router.navigateTo("/boards/${board.id}") } }
        }
    }
