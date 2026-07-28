package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsClose
import com.varabyte.kobweb.silk.components.overlay.Overlay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.boardHistory
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardEventModel
import kotlin.uuid.Uuid

@Composable
fun BoardActivity(
    boardId: Uuid,
    onClose: () -> Unit,
) {
    val events = remember { mutableStateListOf<BoardEventModel>() }
    var page by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(boardId) {
        isLoading = true
        boardHistory(boardId, page = 1).fold(
            { /* handle error */ },
            { response ->
                events.clear()
                events.addAll(response.items)
                hasMore = events.size.toLong() < response.totalItems
            },
        )
        isLoading = false
    }

    fun loadMore() {
        if (!hasMore || isLoading) return
        scope.launch {
            isLoading = true
            boardHistory(boardId, page = page + 1).fold(
                { /* handle error */ },
                { response ->
                    events.addAll(response.items)
                    page = response.page
                    hasMore = events.size.toLong() < response.totalItems
                },
            )
            isLoading = false
        }
    }

    Overlay(
        Modifier
            .backgroundColor(Color.overlay)
            .onClick { onClose() },
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .width(400.px)
                .backgroundColor(Color.surfaceContainerHigh)
                .padding(24.px)
                .border(1.px, LineStyle.Solid, Color.outlineVariant)
                .onClick { it.stopPropagation() },
            Arrangement.spacedBy(24.px),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically,
            ) {
                H2 { Text("Activity") }
                MsClose(
                    Modifier
                        .color(Color.onSurfaceVariant)
                        .cursor(Cursor.Pointer)
                        .onClick { onClose() },
                )
            }

            if (events.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    P { Text("No activity yet.") }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(right = 8.px)
                        .overflow(Overflow.Auto),
                    Arrangement.Top,
                ) {
                    events.forEach { event ->
                        ActivityLogItem(event)
                    }

                    if (hasMore) {
                        Button(
                            { loadMore() },
                            Modifier.fillMaxWidth().margin(top = 16.px, bottom = 16.px),
                            enabled = !isLoading,
                        ) {
                            Text(if (isLoading) "Loading..." else "Load More")
                        }
                    }
                }
            }
        }
    }
}
