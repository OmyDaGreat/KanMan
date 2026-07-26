package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.silk.components.forms.TextInput
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.deleteBoard
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardResponseModel
import xyz.malefic.kanman.shared.data.model.Visibility

@Composable
fun BoardSettings(
    ctx: PageContext,
    board: BoardResponseModel,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf(board.title) }
    var description by remember { mutableStateOf(board.description) }
    var visibility by remember { mutableStateOf(board.visibility) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .backgroundColor(Color.secondaryContainer)
            .padding(32.px)
            .gap(24.px),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .width(600.px)
                .backgroundColor(Color.surfaceContainerHigh)
                .padding(24.px)
                .borderRadius(24.px)
                .gap(24.px),
        ) {
            H2 { Text("Board Settings") }

            // TODO: Add board detail editing backend route (PATCH?)

            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Title") }
                TextInput(
                    title,
                    { title = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "Board Title",
                )
            }

            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Description") }
                TextInput(
                    description,
                    { description = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "Board Description",
                )
            }

            Row(
                Modifier
                    .cursor(Cursor.Pointer)
                    .onClick { visibility = if (visibility == Visibility.PUBLIC) Visibility.PRIVATE else Visibility.PUBLIC }
                    .padding(leftRight = 8.px),
                Arrangement.spacedBy(12.px),
                Alignment.CenterVertically,
            ) {
                Switch(visibility == Visibility.PUBLIC, { /* Handled by the Row's onClick */ })
                P(Modifier.padding(0.px).toAttrs()) {
                    Text("Public Visibility")
                }
            }

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Button(
                    {
                        if (window.confirm("Are you sure you want to delete this board? This action cannot be undone.")) {
                            scope.launch {
                                deleteBoard(board.id).fold(
                                    { /* Handle error */ },
                                    {
                                        ctx.router.navigateTo("/boards/drive")
                                    },
                                )
                            }
                        }
                    },
                    Modifier.backgroundColor(Color.error),
                ) {
                    Text("Delete Board")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.px)) {
                    Button({ onBack() }) {
                        Text("Cancel")
                    }
                    Button(
                        { /* TODO: Update logic when backend is ready */ },
                        enabled = false,
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
