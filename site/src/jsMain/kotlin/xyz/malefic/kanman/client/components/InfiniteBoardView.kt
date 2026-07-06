package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.varabyte.kobweb.browser.uri.encodeURIComponent
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.PageContext
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.shared.data.model.BoardSummaryModel
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.PaginatedResponse

@Composable
fun PageContext.InfiniteBoardView(
    title: String,
    request: suspend (Int, Int) -> Either<Issue, PaginatedResponse<BoardSummaryModel>>,
    onBoardClick: (BoardSummaryModel) -> Unit,
) {
    var page by remember { mutableStateOf(1) }
    var allBoards by remember { mutableStateOf(listOf<BoardSummaryModel>()) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 10
    var error by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    LaunchedEffect(page) {
        request(page, limit).fold(
            { issue ->
                when (issue) {
                    is Issue.Auth -> {
                        router.navigateTo("/login?redirect=${encodeURIComponent(route.path)}")
                    }

                    is Issue.Server.RateLimited -> {
                        error = {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text("Too many requests. Please wait ${issue.retryAfterMs ?: "a moment"}.")
                            }
                        }
                    }

                    else -> {
                        error = {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text("Error: ${issue.message}")
                            }
                        }
                    }
                }
            },
            { response ->
                allBoards = allBoards + response.items
                hasMore = allBoards.size < response.totalItems
            },
        )
    }

    error?.invoke() ?: BoardShowcase(title, allBoards, onBoardClick, hasMore to { page++ })
}
