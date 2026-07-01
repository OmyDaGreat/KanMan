package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.scale
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.icons.mdi.MdiArrowLeft
import com.varabyte.kobweb.silk.components.icons.mdi.MdiArrowRight
import kotlinx.browser.window
import org.jetbrains.compose.web.css.percent
import org.w3c.dom.events.Event
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.PaginatedResponse

@Composable
fun PageContext.PaginatedBoardView(
    title: String,
    request: suspend (Int, Int) -> Either<Issue, PaginatedResponse<BoardSummaryModel>>,
    onBoardClick: (BoardSummaryModel) -> Unit,
) {
    var page by remember { mutableStateOf(1) }
    val windowHeight = remember { mutableStateOf(window.innerHeight) }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = { windowHeight.value = window.innerHeight }
        window.addEventListener("resize", listener)
        onDispose { window.removeEventListener("resize", listener) }
    }

    val limit = remember(windowHeight.value) { ((windowHeight.value - 100) / 120).coerceAtLeast(1) }

    Box(Modifier.fillMaxSize()) {
        Request(page, limit, request = { request(page, limit) }) { response ->
            BoardShowcase(title, response.items, onBoardClick)

            val maxPage = ((response.totalItems + limit - 1) / limit).toInt().coerceAtLeast(1)

            if (page > 1) {
                MdiArrowLeft(
                    Modifier
                        .align(Alignment.BottomStart)
                        .color(Color.onSecondaryContainer)
                        .scale(3)
                        .margin {
                            left(4.percent)
                            bottom(4.percent)
                        }.cursor(Cursor.Pointer)
                        .onClick { page-- },
                )
            }
            if (page < maxPage) {
                MdiArrowRight(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .color(Color.onSecondaryContainer)
                        .scale(3)
                        .margin {
                            right(4.percent)
                            bottom(4.percent)
                        }.cursor(Cursor.Pointer)
                        .onClick { page++ },
                )
            }
        }
    }
}
