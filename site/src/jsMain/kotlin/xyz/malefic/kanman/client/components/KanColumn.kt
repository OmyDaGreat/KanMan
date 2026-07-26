package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onDragOver
import com.varabyte.kobweb.compose.ui.modifiers.onDrop
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsAdd
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.StickyNoteModel
import kotlin.uuid.Uuid
import xyz.malefic.kanman.shared.data.model.Column as BoardColumn

@Composable
fun KanColumn(
    column: BoardColumn,
    stickies: List<StickyNoteModel>,
    canEdit: Boolean = false,
    onAddSticky: () -> Unit = {},
    onMoveSticky: (Uuid) -> Unit = {},
    onDeleteSticky: (Uuid) -> Unit = {},
) = Column(
    Modifier
        .fillMaxHeight()
        .width(20.vw)
        .backgroundColor(Color.primaryContainer)
        .padding(18.px)
        .borderRadius(18.px)
        .overflow(Overflow.Scroll)
        .thenIf(canEdit) {
            Modifier.onDragOver { it.preventDefault() }.onDrop { event ->
                event.dataTransfer?.getData("text/plain")?.let { id ->
                    onMoveSticky(Uuid.parse(id))
                }
            }
        },
    Arrangement.Top,
) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 12.px),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically,
    ) {
        H2 {
            Text(
                column.name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
            )
        }
        if (canEdit) {
            Button({ onAddSticky() }, Modifier.backgroundColor(Colors.Transparent)) {
                MsAdd(Modifier.color(Color.primary))
            }
        }
    }

    val stickyColors = listOf(Color.tertiary, Color.error, Color.primary, Color.secondary)
    stickies.forEach { stickyNote ->
        StickyNote(
            stickyColors.random(),
            stickyNote,
            canEdit,
        ) { onDeleteSticky(stickyNote.id) }
    }
}
