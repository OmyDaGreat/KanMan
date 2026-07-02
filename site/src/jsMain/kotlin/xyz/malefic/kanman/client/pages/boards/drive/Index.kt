package xyz.malefic.kanman.client.pages.boards.drive

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import xyz.malefic.kanman.client.api.getJoinedBoards
import xyz.malefic.kanman.client.components.InfiniteBoardView

@Page
@Composable
fun Drive(ctx: PageContext) =
    InfiniteBoardView("Your Boards", { page, limit -> getJoinedBoards(page, limit) }) { ctx.router.navigateTo("/boards/${it.id}") }
