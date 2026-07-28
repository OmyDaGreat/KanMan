package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
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
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardEventModel
import xyz.malefic.kanman.shared.data.model.WsEvent
import xyz.malefic.kanman.shared.util.toPrettyDateTime

@Composable
fun ActivityLogItem(
    event: BoardEventModel,
    boardTitle: String? = null,
) {
    Row(
        Modifier.fillMaxWidth().padding(topBottom = 8.px).gap(12.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            event.actor.profilePicture,
            "${event.actor.username}'s profile picture",
            Modifier
                .size(32.px)
                .aspectRatio(1)
                .clip(Circle())
                .objectFit(ObjectFit.Cover),
        )
        Column {
            boardTitle?.let {
                P(
                    Modifier
                        .fontSize(10.px)
                        .color(Color.primary)
                        .fontWeight(FontWeight.Bold)
                        .margin(bottom = 2.px)
                        .toAttrs(),
                ) {
                    Text(it.uppercase())
                }
            }
            P(Modifier.margin(0.px).fontSize(14.px).toAttrs()) {
                Text("${event.actor.username} ")
                Text(formatWsEvent(event.event))
            }
            P(
                Modifier
                    .opacity(0.5)
                    .fontSize(12.px)
                    .margin(0.px)
                    .toAttrs(),
            ) {
                Text(event.timestamp.toPrettyDateTime())
            }
        }
    }
}

private fun formatWsEvent(event: WsEvent): String =
    when (event) {
        is WsEvent.StickyCreated -> "created a new sticky: ${event.sticky.title}"
        is WsEvent.StickyMoved -> "moved a sticky to ${event.newColumn.name.lowercase()}"
        is WsEvent.StickyDeleted -> "deleted a sticky"
        is WsEvent.StickyUpdated -> "updated sticky: ${event.sticky.title}"
        is WsEvent.AssignedUser -> "assigned ${event.target.username} to a sticky"
        is WsEvent.UnassignedUser -> "unassigned ${event.target.username} from a sticky"
        is WsEvent.UserOpenBoard -> "opened the board"
        is WsEvent.UserCloseBoard -> "closed the board"
        is WsEvent.BoardLoad -> "loaded the board"
    }
