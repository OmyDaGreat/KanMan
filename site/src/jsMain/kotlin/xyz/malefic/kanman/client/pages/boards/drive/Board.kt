package xyz.malefic.kanman.client.pages.boards.drive

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import org.jetbrains.compose.web.css.px
import xyz.malefic.kanman.client.api.getBoard
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.components.KanColumn
import xyz.malefic.kanman.client.components.Spinner
import xyz.malefic.kanman.shared.data.model.Column
import kotlin.uuid.Uuid

@Page("{id}")
@Composable
fun Board(ctx: PageContext) {
    val boardId = ctx.route.params["id"]?.let { Uuid.parse(it) } ?: return Spinner()

    ctx.Request(boardId, request = { getBoard(boardId) }) { board ->
        Row(
            Modifier.fillMaxSize().padding(12.px),
            Arrangement.SpaceEvenly,
            Alignment.CenterVertically,
        ) {
            KanColumn("Backlog", board.stickies.filter { it.column == Column.BACKLOG })
            KanColumn("Planning", board.stickies.filter { it.column == Column.PLANNING })
            KanColumn("In Progress", board.stickies.filter { it.column == Column.IN_PROGRESS })
            KanColumn("Done", board.stickies.filter { it.column == Column.DONE })
        }
    }
}
