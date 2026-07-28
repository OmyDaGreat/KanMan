package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.browser.dom.ElementTarget
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.ms.MsPersonAdd
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.UserSummaryModel
import xyz.malefic.kutint.Kutint
import kotlin.uuid.Uuid

@Composable
fun UserAvatarRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.px),
    color: Kutint<*> = Color.primary,
    assignedUsers: List<UserSummaryModel>,
    canEdit: Boolean = false,
    onAddClick: () -> Unit = {},
    onUserClick: (Uuid) -> Unit = {},
) {
    Row(modifier, horizontalArrangement, Alignment.CenterVertically) {
        assignedUsers.forEachIndexed { index, user ->
            Box(
                Modifier
                    .size(24.px)
                    .aspectRatio(1)
                    .zIndex(assignedUsers.size - index),
            ) {
                Image(
                    user.profilePicture,
                    "${user.username}'s profile picture",
                    Modifier
                        .fillMaxSize()
                        .clip(Circle())
                        .border(2.px, LineStyle.Solid, Colors.White)
                        .thenIf(canEdit) {
                            Modifier
                                .cursor(Cursor.Pointer)
                                .onClick { onUserClick(user.id) }
                        },
                )
            }
            Tooltip(ElementTarget.PreviousSibling, user.username)
        }
        if (canEdit) {
            Button(
                { onAddClick() },
                Modifier
                    .backgroundColor(Colors.Transparent)
                    .zIndex(0)
                    .padding(0.px),
            ) {
                MsPersonAdd(Modifier.color(color).fontSize(24.px))
            }
        }
    }
}
