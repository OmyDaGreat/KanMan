package xyz.malefic.kanman.client.pages.boards.drive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsAddAd
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.getJoinedBoards
import xyz.malefic.kanman.client.components.CreateBoardOverlay
import xyz.malefic.kanman.client.components.InfiniteBoardView
import xyz.malefic.kanman.client.styles.Color

@Page
@Composable
fun Drive(ctx: PageContext) {
    var showCreatePopup by remember { mutableStateOf(false) }

    ctx.InfiniteBoardView(
        "Your Boards",
        { page, limit -> getJoinedBoards(page, limit) },
        { ctx.router.navigateTo("/boards/drive/${it.id}") },
        {
            Button({ showCreatePopup = true }, Modifier.backgroundColor(Colors.Transparent)) {
                MsAddAd(Modifier.color(Color.primary))
                P(Modifier.padding(left = 8.px).color(Color.primary).toAttrs()) {
                    Text("Create Board")
                }
            }
        },
    )

    if (showCreatePopup) {
        CreateBoardOverlay(ctx, onClose = { showCreatePopup = false })
    }
}
