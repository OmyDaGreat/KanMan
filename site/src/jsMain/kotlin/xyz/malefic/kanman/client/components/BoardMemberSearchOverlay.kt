package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.UserSummaryModel
import kotlin.uuid.Uuid

@Composable
fun BoardMemberSearchOverlay(
    members: List<UserSummaryModel>,
    alreadyAssigned: List<Uuid>,
    onClose: () -> Unit,
    onSubmit: (Uuid) -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val filteredMembers =
        members.filter {
            it.id !in alreadyAssigned && it.username.contains(search, ignoreCase = true)
        }

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
            H2 { Text("Assign Board Member") }

            TextInput(
                search,
                { search = it },
                Modifier.fillMaxWidth(),
                placeholder = "Search members...",
            )

            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.px)) {
                if (filteredMembers.isEmpty()) {
                    P { Text("No members found.") }
                }
                filteredMembers.forEach { member ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.px)
                            .borderRadius(8.px)
                            .cursor(Cursor.Pointer)
                            .onClick {
                                onSubmit(member.id)
                                onClose()
                            },
                        Arrangement.spacedBy(12.px),
                        Alignment.CenterVertically,
                    ) {
                        Image(
                            member.profilePicture,
                            "${member.username}'s profile picture",
                            Modifier.size(32.px).clip(Circle()),
                        )
                        Text(member.username)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                Button(
                    { onClose() },
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
