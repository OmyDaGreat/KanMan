package xyz.malefic.kanman.client.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
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
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.icons.ms.MsCheckCircle
import com.varabyte.kobweb.silk.components.icons.ms.MsIconStyle
import com.varabyte.kobweb.silk.components.icons.ms.MsSend
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.acceptInvitation
import xyz.malefic.kanman.client.api.getBoard
import xyz.malefic.kanman.client.api.getInvitations
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.components.handle
import xyz.malefic.kanman.client.styles.Color

@OptIn(DelicateCoroutinesApi::class)
@Page
@Composable
fun Inbox(ctx: PageContext) =
    with(ctx) {
        Request(request = { getInvitations() }) { invitations ->
            Column(
                Modifier
                    .fillMaxSize()
                    .backgroundColor(Color.secondaryContainer)
                    .padding(32.px, 10.percent)
                    .gap(24.px),
            ) {
                H1 { Text("Invitations") }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1)
                        .overflow(Overflow.Auto)
                        .gap(12.px)
                        .padding(8.px),
                ) {
                    invitations.forEach { invitation ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.px)
                                .borderRadius(16.px)
                                .backgroundColor(Color.surfaceContainer),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically,
                        ) {
                            MsSend(Modifier.fontSize(72.px), MsIconStyle.ROUNDED)
                            Column(Modifier.gap(4.px)) {
                                Request(request = { getBoard(invitation.boardId) }) { board ->
                                    H3 { Text(board.title) }
                                }
                                Request(request = { getUser(invitation.senderId) }) { sender ->
                                    P(Modifier.opacity(0.7).toAttrs()) { Text(sender.username) }
                                }
                            }
                            MsCheckCircle(
                                Modifier.fontSize(72.px).cursor(Cursor.Pointer).onClick {
                                    GlobalScope.launch {
                                        handle(acceptInvitation(invitation.id)) {
                                            router.navigateTo("/boards/${invitation.boardId}")
                                        }
                                    }
                                },
                                MsIconStyle.ROUNDED,
                            )
                        }
                    }
                }
            }
        }
    }
