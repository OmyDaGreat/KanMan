package xyz.malefic.kanman.client.pages.boards

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import xyz.malefic.kanman.client.api.getJoinedBoards
import xyz.malefic.kanman.client.api.util.Request

@Page
@Composable
fun Drive(ctx: PageContext) =
    ctx.Request(request = { getJoinedBoards() }) { boards ->
        Column {
            boards.items.forEach { board ->
                TODO()
            }
        }
    }
