package xyz.malefic.kanman.client.pages.boards.drive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsSettings
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.getBoard
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.components.BoardSettings
import xyz.malefic.kanman.client.components.KanColumn
import xyz.malefic.kanman.client.components.Spinner
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.Column
import xyz.malefic.kanman.shared.data.model.Role
import kotlin.uuid.Uuid

@Page("{id}")
@Composable
fun Board(ctx: PageContext) {
    val boardId = ctx.route.params["id"]?.let { Uuid.parse(it) } ?: return Spinner()
    var isSettingsView by remember { mutableStateOf(false) }

    Request(boardId, request = { getBoard(boardId) }) { board ->
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.surfaceContainer)
                    .padding(16.px, 24.px),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically,
            ) {
                H1 { Text(board.title) }

                Request(request = { getUser() }) { user ->
                    val role = board.memberships.find { it.user.id == user.id }?.role
                    if (role == Role.OWNER || role == Role.ADMIN) {
                        Button({ isSettingsView = !isSettingsView }, Modifier.backgroundColor(Colors.Transparent)) {
                            MsSettings(Modifier.color(Color.primary))
                        }
                    }
                }
            }

            if (isSettingsView) {
                BoardSettings(ctx, board, onBack = { isSettingsView = false })
            } else {
                Row(
                    Modifier.fillMaxSize().padding(12.px).gap(12.px),
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
    }
}
