package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.browser.dom.ElementTarget
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.ms.MsPersonAdd
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.AssignedUserModel
import xyz.malefic.kanman.shared.data.model.StickyNoteModel
import xyz.malefic.kanman.shared.data.model.UserSummaryModel

@Composable
fun StickyOverlay(
    sticky: StickyNoteModel? = null,
    onClose: () -> Unit,
    onConfirm: (title: String, content: String?, assignedUsers: List<AssignedUserModel>) -> Unit,
) {
    var title by remember { mutableStateOf(sticky?.title ?: "") }
    var content by remember { mutableStateOf(sticky?.content ?: "") }
    val assignedUsers = remember { mutableStateListOf(*sticky?.assignedUsers?.toTypedArray() ?: emptyArray()) }
    var showUserPopup by remember { mutableStateOf(false) }

    Overlay(
        Modifier.backgroundColor(Color.overlay),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .backgroundColor(Color.surfaceContainerHigh)
                .padding(24.px)
                .borderRadius(24.px)
                .border(1.px, LineStyle.Solid, Color.outlineVariant)
                .width(400.px),
            Arrangement.spacedBy(24.px),
        ) {
            H2 { Text(if (sticky == null) "Create New Sticky" else "Edit Sticky") }

            Column(Modifier.fillMaxWidth().padding(bottom = 8.px), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Title") }
                TextInput(
                    title,
                    { title = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "Sticky Title",
                )
            }

            Column(Modifier.fillMaxWidth().padding(bottom = 8.px), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Content") }
                TextArea(
                    content,
                    Modifier
                        .fillMaxWidth()
                        .height(150.px)
                        .padding(12.px)
                        .borderRadius(8.px)
                        .border(1.px, LineStyle.Solid, Color.outlineVariant)
                        .backgroundColor(Colors.Transparent)
                        .color(Color.onSurface)
                        .toAttrs {
                            onInput { content = it.value }
                            placeholder("Sticky Content (optional)")
                        },
                )
            }

            Row(Modifier.fillMaxWidth().padding(bottom = 8.px), Arrangement.spacedBy(8.px)) {
                assignedUsers.forEachIndexed { index, model ->
                    var user by remember(model.userId) { mutableStateOf<UserSummaryModel?>(null) }
                    LaunchedEffect(model.userId) {
                        handle(getUser(model.userId)) {
                            user = it
                        }
                    }

                    Box(
                        Modifier
                            .size(12.px)
                            .aspectRatio(1)
                            .thenIf(index > 0) { Modifier.margin(left = (-6).px) }
                            .zIndex(assignedUsers.size - index),
                    ) {
                        Image(
                            user?.profilePicture ?: "https://i.pinimg.com/736x/0f/c2/b0/0fc2b09b645e3f878ce39232ec3e552e.jpg",
                            "${user?.username ?: model.userId}'s profile picture",
                            Modifier
                                .fillMaxSize()
                                .clip(Circle())
                                .border(2.px, LineStyle.Solid, Colors.White)
                                .cursor(Cursor.Pointer)
                                .onClick {
                                    assignedUsers.removeAll { it.userId == model.userId }
                                },
                        )
                    }
                    Tooltip(ElementTarget.PreviousSibling, user?.username ?: model.userId.toString())
                }
                Button({
                    showUserPopup = true
                }, Modifier.size(12.px).aspectRatio(1)) {
                    MsPersonAdd(Modifier.color(Color.primary).size(8.px))
                }
            }

            Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                Button(
                    { onClose() },
                    Modifier.margin(right = 8.px),
                ) {
                    Text("Cancel")
                }
                Button(
                    {
                        onConfirm(title, content.ifBlank { null }, assignedUsers)
                        onClose()
                    },
                    enabled = title.isNotBlank(),
                ) {
                    Text(if (sticky == null) "Create" else "Save")
                }
            }
        }
    }

    if (showUserPopup) {
        UserSearchOverlay({ showUserPopup = false }, {
            assignedUsers += AssignedUserModel(it)
        })
    }
}
