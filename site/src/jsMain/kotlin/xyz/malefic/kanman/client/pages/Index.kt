package xyz.malefic.kanman.client.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsAdd
import com.varabyte.kobweb.silk.components.navigation.Link
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.acceptInvitation
import xyz.malefic.kanman.client.api.declineInvitation
import xyz.malefic.kanman.client.api.getInvitations
import xyz.malefic.kanman.client.api.getJoinedBoards
import xyz.malefic.kanman.client.api.getUserHistory
import xyz.malefic.kanman.client.api.getUserTasks
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.components.ActivityLogItem
import xyz.malefic.kanman.client.components.BoardCard
import xyz.malefic.kanman.client.components.BoardCreationOverlay
import xyz.malefic.kanman.client.components.EmptyState
import xyz.malefic.kanman.client.components.InvitationItem
import xyz.malefic.kanman.client.components.SectionCard
import xyz.malefic.kanman.client.components.TaskSummaryItem
import xyz.malefic.kanman.client.components.handle
import xyz.malefic.kanman.client.styles.Color

@Page
@Composable
fun Index() {
    val ctx = rememberPageContext()
    val scope = rememberCoroutineScope()
    var showCreatePopup by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().backgroundColor(Color.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.px, 10.percent)
                .gap(32.px)
                .overflow(Overflow.Auto),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically,
            ) {
                H1 { Text("Dashboard") }
                Button(
                    { showCreatePopup = true },
                    Modifier
                        .backgroundColor(Color.primary)
                        .color(Color.onPrimary)
                        .borderRadius(12.px)
                        .padding(8.px, 16.px),
                ) {
                    MsAdd(Modifier.margin(right = 8.px))
                    Text("New Board")
                }
            }

            Row(Modifier.fillMaxWidth().gap(32.px)) {
                Column(Modifier.weight(0.35f).gap(24.px)) {
                    Request(request = { getInvitations() }) { invitations ->
                        if (invitations.isNotEmpty()) {
                            SectionCard("Invitations") {
                                Column(Modifier.gap(8.px)) {
                                    invitations.forEach { invite ->
                                        InvitationItem(
                                            invite,
                                            onAccept = {
                                                scope.launch {
                                                    handle(acceptInvitation(invite.id)) {
                                                        ctx.router.navigateTo("/boards/drive/${invite.board.id}")
                                                    }
                                                }
                                            },
                                            onDecline = {
                                                scope.launch {
                                                    handle(declineInvitation(invite.id)) {
                                                        window.location.reload()
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Request(request = { getJoinedBoards(limit = 10) }) { response ->
                        SectionCard("Your Boards", action = {
                            Link("/boards/drive", "View All", Modifier.color(Color.primary).fontSize(14.px))
                        }) {
                            if (response.items.isEmpty()) {
                                EmptyState("You haven't joined any boards yet.")
                            } else {
                                Column(Modifier.gap(8.px)) {
                                    response.items.forEach { board ->
                                        BoardCard(board) { ctx.router.navigateTo("/boards/drive/${board.id}") }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(Modifier.weight(0.65f).gap(24.px)) {
                    Request(request = { getJoinedBoards(limit = 100) }) { response ->
                        val boardMap = response.items.associate { it.id to it.title }
                        Request(request = { getUserTasks() }) { tasks ->
                            SectionCard("Upcoming Tasks") {
                                if (tasks.isEmpty()) {
                                    EmptyState("No upcoming tasks. Enjoy your day!")
                                } else {
                                    Column(Modifier.gap(16.px)) {
                                        tasks.groupBy { it.boardId }.forEach { (boardId, boardTasks) ->
                                            val boardTitle = boardMap[boardId] ?: "Unknown Board"
                                            boardTasks.forEach { task ->
                                                TaskSummaryItem(task, boardTitle) {
                                                    ctx.router.navigateTo("/boards/drive/${task.boardId}")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Request(request = { getUserHistory(limit = 20) }) { historyResponse ->
                            SectionCard("Recent Activity") {
                                if (historyResponse.items.isEmpty()) {
                                    EmptyState("No recent activity.")
                                } else {
                                    Column(Modifier.gap(4.px)) {
                                        historyResponse.items.groupBy { it.boardId }.forEach { (boardId, boardEvents) ->
                                            val boardTitle = boardMap[boardId] ?: "Unknown Board"
                                            boardEvents.forEach { event ->
                                                ActivityLogItem(event, boardTitle)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreatePopup) {
            BoardCreationOverlay(onClose = { showCreatePopup = false })
        }
    }
}
