package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
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
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsArrowDownward
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel

@Composable
fun BoardShowcase(
    title: String,
    boards: List<BoardSummaryModel>,
    onBoardClick: (BoardSummaryModel) -> Unit,
    hasMore: Pair<Boolean, (SyntheticMouseEvent) -> Unit>,
    extraHeaderActions: @Composable () -> Unit = {},
) = Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .backgroundColor(Color.secondaryContainer)
            .padding(32.px, 10.percent)
            .gap(24.px),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            H1 { Text(title) }
            extraHeaderActions()
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1)
                .overflow(Overflow.Auto)
                .gap(12.px)
                .padding(8.px),
        ) {
            if (boards.isEmpty()) {
                EmptyState("No boards found.")
            } else {
                boards.forEach { board ->
                    BoardCard(board, isDetailed = true) { onBoardClick(board) }
                }
            }
        }
    }

    if (hasMore.first) {
        Button(hasMore.second, Modifier.align(Alignment.BottomEnd).margin(32.px, 10.percent).backgroundColor(Colors.Transparent)) {
            Row {
                MsArrowDownward(Modifier.top(135.percent).color(Color.onSecondaryContainer))
                P(Modifier.color(Color.onSecondaryContainer).toAttrs()) { Text("Load more") }
            }
        }
    }
}
