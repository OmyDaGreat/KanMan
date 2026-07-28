package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.objectFit
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.ms.MsCancel
import com.varabyte.kobweb.silk.components.icons.ms.MsCheck
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.Invitation

@Composable
fun InvitationItem(
    invitation: Invitation,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(16.px)
            .borderRadius(16.px)
            .backgroundColor(Color.surfaceContainerHigh),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            Row(Modifier.gap(12.px), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    invitation.sender.profilePicture,
                    "${invitation.sender.username}'s profile picture",
                    Modifier
                        .size(40.px)
                        .aspectRatio(1)
                        .clip(Circle())
                        .objectFit(ObjectFit.Cover),
                )
                Column {
                    P(Modifier.fontWeight(FontWeight.Bold).margin(0.px).toAttrs()) {
                        Text(invitation.board.title)
                    }
                    P(
                        Modifier
                            .fontSize(13.px)
                            .opacity(0.8)
                            .margin(0.px)
                            .toAttrs(),
                    ) {
                        Text("From ${invitation.sender.username}")
                    }
                }
            }
            Row(Modifier.gap(8.px), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    { onDecline() },
                    Modifier.backgroundColor(Colors.Transparent).color(Color.error),
                ) {
                    MsCancel()
                }
                Button(
                    { onAccept() },
                    Modifier.backgroundColor(Color.primary).color(Color.onPrimary),
                ) {
                    MsCheck()
                }
            }
        }
    }
}
