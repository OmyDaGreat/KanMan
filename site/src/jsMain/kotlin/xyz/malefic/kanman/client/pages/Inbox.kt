package xyz.malefic.kanman.client.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.ms.MsCancel
import com.varabyte.kobweb.silk.components.icons.ms.MsCheckCircle
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.acceptInvitation
import xyz.malefic.kanman.client.api.declineInvitation
import xyz.malefic.kanman.client.api.getInvitations
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.components.handle
import xyz.malefic.kanman.client.styles.Color

@Page
@Composable
fun Inbox(ctx: PageContext) {
    val scope = rememberCoroutineScope()

    Request(request = { getInvitations() }) { invitations ->
        Column(
            Modifier
                .fillMaxSize()
                .backgroundColor(Color.secondaryContainer)
                .padding(32.px, 10.percent)
                .gap(24.px),
        ) {
            H1 { Text("Invitations") }

            if (invitations.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().margin(top = 48.px),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    P(
                        Modifier
                            .opacity(0.6)
                            .textAlign(TextAlign.Center)
                            .fontSize(20.px)
                            .toAttrs(),
                    ) {
                        Text("You don't have any pending invitations.")
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1)
                        .overflow(Overflow.Auto)
                        .gap(16.px)
                        .padding(8.px),
                ) {
                    invitations.forEach { invitation ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.px)
                                .borderRadius(24.px)
                                .backgroundColor(Color.surfaceContainerHigh),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically,
                        ) {
                            Row(Modifier, Arrangement.spacedBy(20.px), Alignment.CenterVertically) {
                                Image(
                                    invitation.sender.profilePicture,
                                    "${invitation.sender.username}'s profile picture",
                                    Modifier.size(64.px).aspectRatio(1).clip(Circle()),
                                )
                                Column(Modifier.gap(8.px)) {
                                    H3 { Text(invitation.board.title) }
                                    P(Modifier.opacity(0.8).toAttrs()) {
                                        Text("${invitation.sender.username} invited you to join as ${invitation.role.name.lowercase()}.")
                                    }
                                    if (invitation.board.description.isNotBlank()) {
                                        P(Modifier.opacity(0.6).fontSize(14.px).toAttrs()) {
                                            Text(invitation.board.description)
                                        }
                                    }
                                }
                            }

                            Row(Modifier, Arrangement.spacedBy(12.px)) {
                                Button(
                                    {
                                        scope.launch {
                                            handle(declineInvitation(invitation.id)) {
                                                window.location.reload()
                                            }
                                        }
                                    },
                                    Modifier
                                        .backgroundColor(Color.surfaceContainerLowest)
                                        .color(Color.error)
                                        .padding(topBottom = 0.px, leftRight = 12.px),
                                ) {
                                    MsCancel(Modifier.margin(right = 8.px))
                                    Text("Decline")
                                }
                                Button(
                                    {
                                        scope.launch {
                                            handle(acceptInvitation(invitation.id)) {
                                                ctx.router.navigateTo("/boards/drive/${invitation.board.id}")
                                            }
                                        }
                                    },
                                    Modifier.padding(topBottom = 0.px, leftRight = 12.px),
                                ) {
                                    MsCheckCircle(Modifier.margin(right = 8.px))
                                    Text("Accept")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
