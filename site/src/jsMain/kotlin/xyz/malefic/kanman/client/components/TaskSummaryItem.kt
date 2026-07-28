package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextOverflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textOverflow
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.Surface
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.StickyNoteModel
import xyz.malefic.kanman.shared.util.isOverdue
import xyz.malefic.kanman.shared.util.toPrettyDate

@Composable
fun TaskSummaryItem(
    sticky: StickyNoteModel,
    boardTitle: String,
    onClick: () -> Unit,
) {
    val due = sticky.assignedUsers.mapNotNull { it.due }.minOrNull()

    Surface(
        Modifier
            .fillMaxWidth()
            .padding(16.px)
            .borderRadius(12.px)
            .backgroundColor(Color.surfaceContainerLow)
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            Column(Modifier.gap(4.px).weight(1)) {
                P(
                    Modifier
                        .fontSize(12.px)
                        .color(Color.primary)
                        .fontWeight(FontWeight.Bold)
                        .toAttrs(),
                ) {
                    Text(boardTitle.uppercase())
                }
                H3(Modifier.margin(0.px).toAttrs()) { Text(sticky.title) }
                if (sticky.content.isNotBlank()) {
                    P(
                        Modifier
                            .opacity(0.7)
                            .fontSize(14.px)
                            .textOverflow(TextOverflow.Ellipsis)
                            .toAttrs(),
                    ) {
                        Text(sticky.content)
                    }
                }
            }

            due?.let {
                Column(horizontalAlignment = Alignment.End) {
                    P(Modifier.fontSize(12.px).color(if (it.isOverdue()) Color.error else Color.onSurfaceVariant).toAttrs()) {
                        Text("Due: ${it.toPrettyDate()}")
                    }
                }
            }
        }
    }
}
