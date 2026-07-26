package xyz.malefic.kanman.client.pages.boards.drive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
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
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsSettings
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.WebSocket
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.api.util.AuthSession
import xyz.malefic.kanman.client.api.util.GlobalErrorState
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.api.util.WebSockets
import xyz.malefic.kanman.client.components.BoardSettings
import xyz.malefic.kanman.client.components.KanColumn
import xyz.malefic.kanman.client.components.Spinner
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardResponseModel
import xyz.malefic.kanman.shared.data.model.Column
import xyz.malefic.kanman.shared.data.model.Role
import xyz.malefic.kanman.shared.data.model.WsEvent
import xyz.malefic.kanman.shared.data.model.WsEvent.BoardLoad
import xyz.malefic.kanman.shared.data.model.WsEvent.StickyCreated
import xyz.malefic.kanman.shared.data.model.WsEvent.StickyDeleted
import xyz.malefic.kanman.shared.data.model.WsEvent.StickyMoved
import kotlin.uuid.Uuid

@Page("{id}")
@Composable
fun Board(ctx: PageContext) {
    val boardId = ctx.route.params["id"]?.let { Uuid.parse(it) } ?: return Spinner()
    var isSettingsView by remember { mutableStateOf(false) }
    var board by remember { mutableStateOf<BoardResponseModel?>(null) }
    var websocket by remember { mutableStateOf<WebSocket?>(null) }

    DisposableEffect(boardId, AuthSession.accessToken) {
        websocket =
            WebSockets.connect(boardId) { result ->
                result.fold(
                    { issue -> GlobalErrorState.show(issue) },
                    { event ->
                        when (event) {
                            is BoardLoad -> {
                                board = event.board
                            }

                            is StickyMoved -> {
                                board =
                                    board?.let { b ->
                                        b.copy(
                                            stickies =
                                                b.stickies.map { s ->
                                                    if (s.id == event.stickyId) s.copy(column = event.newColumn) else s
                                                },
                                        )
                                    }
                            }

                            is StickyCreated -> {
                                board = board?.let { b -> b.copy(stickies = b.stickies + event.sticky) }
                            }

                            is StickyDeleted -> {
                                board = board?.let { b -> b.copy(stickies = b.stickies.filter { it.id != event.stickyId }) }
                            }

                            is WsEvent.AssignedUser -> {
                                TODO()
                            }

                            is WsEvent.UnassignedUser -> {
                                TODO()
                            }

                            is WsEvent.UserJoin -> {
                                TODO()
                            }

                            is WsEvent.UserLeave -> {
                                TODO()
                            }
                        }
                    },
                )
            }
        onDispose {
            websocket?.close()
        }
    }

    val currentBoard = board ?: return Spinner()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .backgroundColor(Color.surfaceContainer)
                .padding(16.px, 24.px),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            H1 { Text(currentBoard.title) }

            Request(request = { getUser() }) { user ->
                val role = currentBoard.memberships.find { it.user.id == user.id }?.role
                if (role == Role.OWNER || role == Role.ADMIN) {
                    Button({ isSettingsView = !isSettingsView }, Modifier.backgroundColor(Colors.Transparent)) {
                        MsSettings(Modifier.color(Color.primary))
                    }
                }
            }
        }

        if (isSettingsView) {
            BoardSettings(currentBoard, onBack = { isSettingsView = false })
        } else {
            Row(
                Modifier.fillMaxSize().padding(12.px).gap(12.px),
                Arrangement.SpaceEvenly,
                Alignment.CenterVertically,
            ) {
                KanColumn("Backlog", currentBoard.stickies.filter { it.column == Column.BACKLOG })
                KanColumn("Planning", currentBoard.stickies.filter { it.column == Column.PLANNING })
                KanColumn("In Progress", currentBoard.stickies.filter { it.column == Column.IN_PROGRESS })
                KanColumn("Done", currentBoard.stickies.filter { it.column == Column.DONE })
            }
        }
    }
}
