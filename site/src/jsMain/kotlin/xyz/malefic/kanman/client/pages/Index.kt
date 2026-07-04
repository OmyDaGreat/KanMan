package xyz.malefic.kanman.client.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.navigation.Link
import xyz.malefic.kanman.client.api.createBoard
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.shared.data.model.BoardCreateModel
import xyz.malefic.kanman.shared.data.model.Visibility

@Page
@Composable
fun Index(ctx: PageContext) =
    ctx.Request(request = { createBoard(BoardCreateModel("Test", "", Visibility.PUBLIC)) }) { board ->
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Link(
                path = "/boards/${board.id}",
                text = "Go to Board: ${board.title}",
            )
        }
    }
