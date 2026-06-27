package xyz.malefic.kanman.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import arrow.core.getOrElse
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import xyz.malefic.kanman.api.board
import xyz.malefic.kanman.data.model.BoardCreateModel
import xyz.malefic.kanman.data.model.BoardResponseModel
import xyz.malefic.kanman.data.model.Visibility

@Page
@Composable
fun HomePage() {
    val board by produceState<BoardResponseModel?>(initialValue = null) {
        value = board(BoardCreateModel("Test", Visibility.PUBLIC)).getOrElse { null }
    }

    if (board == null) {
        // TODO: Handle loading state
        return
    }

    Box(Modifier.fillMaxSize()) {
        Link(
            path = "/board/${board!!.id}",
            text = "Go to Board: ${board!!.title}",
        )
    }
}
