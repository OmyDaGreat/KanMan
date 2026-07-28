package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.Surface
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel
import xyz.malefic.kanman.shared.data.model.Visibility

@Composable
fun BoardCard(
    board: BoardSummaryModel,
    isDetailed: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(if (isDetailed) 16.px else 12.px)
            .borderRadius(16.px)
            .backgroundColor(if (isDetailed) Color.surfaceContainer else Color.surfaceContainerLow)
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.px),
            verticalAlignment = if (isDetailed) Alignment.Top else Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(if (isDetailed) 12.px else 8.px)
                    .margin(top = if (isDetailed) 6.px else 0.px)
                    .borderRadius(50.percent)
                    .backgroundColor(if (board.visibility == Visibility.PUBLIC) Color.primary else Color.tertiary),
            )
            Column(Modifier.gap(4.px)) {
                H3(Modifier.margin(0.px).toAttrs()) { Text(board.title) }
                if (isDetailed && board.description.isNotBlank()) {
                    P(Modifier.margin(0.px).toAttrs()) { Text(board.description) }
                }
                P(
                    Modifier
                        .fontSize(12.px)
                        .opacity(0.7)
                        .margin(0.px)
                        .toAttrs(),
                ) { Text(board.owner.username) }
            }
        }
    }
}
