package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.overlay.Overlay
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.AssignedUserModel
import xyz.malefic.kanman.shared.data.model.StickyNoteModel
import xyz.malefic.kanman.shared.data.model.UserSummaryModel
import xyz.malefic.kanman.shared.util.toInstant
import xyz.malefic.kanman.shared.util.toPrettyDate

@Composable
fun StickyOverlay(
    sticky: StickyNoteModel? = null,
    members: List<UserSummaryModel>,
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

            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.px)) {
                UserAvatarRow(
                    Modifier.fillMaxWidth(),
                    Arrangement.Start,
                    assignedUsers =
                        assignedUsers.mapNotNull { assigned ->
                            members.find { it.id == assigned.userId }
                        },
                    dueDates = assignedUsers.associate { it.userId to it.due },
                    canEdit = true,
                    onAddClick = { showUserPopup = true },
                    onUserClick = { userId ->
                        assignedUsers.removeAll { it.userId == userId }
                    },
                )

                if (assignedUsers.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth().gap(8.px)) {
                        P(Modifier.padding(0.px).toAttrs()) { Text("Deadlines") }
                        assignedUsers.forEach { assigned ->
                            val user = members.find { it.id == assigned.userId } ?: return@forEach
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(user.username)
                                Input(
                                    type = InputType.Date,
                                    attrs =
                                        Modifier
                                            .padding(4.px)
                                            .borderRadius(4.px)
                                            .border(1.px, LineStyle.Solid, Color.outlineVariant)
                                            .backgroundColor(Colors.Transparent)
                                            .color(Color.onSurface)
                                            .toAttrs {
                                                value(assigned.due?.toPrettyDate() ?: "")
                                                onInput { event ->
                                                    val index = assignedUsers.indexOf(assigned)
                                                    if (index != -1) {
                                                        assignedUsers[index] =
                                                            assigned.copy(
                                                                due = event.value.takeIf { it.isNotBlank() }?.toInstant(),
                                                            )
                                                    }
                                                }
                                            },
                                )
                            }
                        }
                    }
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
        BoardMemberSearchOverlay(
            members = members,
            alreadyAssigned = assignedUsers.map { it.userId },
            onClose = { showUserPopup = false },
            onSubmit = {
                assignedUsers += AssignedUserModel(it)
            },
        )
    }
}
