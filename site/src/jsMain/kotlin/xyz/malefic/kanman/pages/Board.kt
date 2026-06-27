package xyz.malefic.kanman.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import org.jetbrains.compose.web.css.px
import xyz.malefic.kanman.api.board
import xyz.malefic.kanman.api.util.Request
import xyz.malefic.kanman.components.KanColumn
import xyz.malefic.kanman.data.model.Column
import kotlin.uuid.Uuid

@Page
@Composable
fun BoardPage() {
    val ctx = rememberPageContext()
    val boardId = ctx.route.params["id"]?.let { Uuid.parse(it) } ?: return

    Request(boardId, request = { board(boardId) }) { board ->
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
