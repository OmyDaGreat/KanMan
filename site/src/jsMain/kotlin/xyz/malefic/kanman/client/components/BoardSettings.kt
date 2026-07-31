package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.ms.MsPersonAdd
import com.varabyte.kobweb.silk.components.icons.ms.MsPersonRemove
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.declineInvitation
import xyz.malefic.kanman.client.api.deleteBoard
import xyz.malefic.kanman.client.api.getBoardInvitations
import xyz.malefic.kanman.client.api.invite
import xyz.malefic.kanman.client.api.kick
import xyz.malefic.kanman.client.api.updateBoard
import xyz.malefic.kanman.client.api.updateRole
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.BoardDetailsModel
import xyz.malefic.kanman.shared.data.model.BoardResponseModel
import xyz.malefic.kanman.shared.data.model.Role
import xyz.malefic.kanman.shared.data.model.Visibility

@Composable
fun BoardSettings(
    board: BoardResponseModel,
    currentRole: Role,
    onBack: () -> Unit,
) {
    val ctx = rememberPageContext()
    var title by remember { mutableStateOf(board.title) }
    var description by remember { mutableStateOf(board.description) }
    var visibility by remember { mutableStateOf(board.visibility) }
    var isUserSearchOpen by remember { mutableStateOf(false) }
    var isTransferSearchOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val hasChanges = title != board.title || description != board.description || visibility != board.visibility

    Column(
        Modifier
            .fillMaxSize()
            .backgroundColor(Color.secondaryContainer)
            .padding(32.px)
            .gap(24.px),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .width(600.px)
                .backgroundColor(Color.surfaceContainerHigh)
                .padding(24.px)
                .borderRadius(24.px)
                .gap(24.px),
        ) {
            H2 { Text("Board Settings") }

            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Title") }
                TextInput(
                    title,
                    { title = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "Board Title",
                )
            }

            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Description") }
                TextInput(
                    description,
                    { description = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "Board Description",
                )
            }

            Switch(
                visibility == Visibility.PUBLIC,
                { visibility = if (it) Visibility.PUBLIC else Visibility.PRIVATE },
            ) {
                Text("Public Visibility")
            }

            HorizontalDivider(Modifier.fillMaxWidth())

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                H3 { Text("Members") }
                Button(
                    { isUserSearchOpen = true },
                    Modifier.backgroundColor(Colors.Transparent).padding(0.px),
                ) {
                    MsPersonAdd(Modifier.color(Color.primary).fontSize(24.px))
                }
            }

            Column(Modifier.fillMaxWidth().gap(12.px)) {
                board.memberships.forEach { member ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.px)
                            .backgroundColor(Color.surfaceContainer)
                            .borderRadius(12.px),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically,
                    ) {
                        Row(Modifier, Arrangement.spacedBy(12.px), Alignment.CenterVertically) {
                            Image(
                                member.user.profilePicture,
                                "${member.user.username}'s profile picture",
                                Modifier.size(32.px).clip(Circle()),
                            )
                            Text(member.user.username)
                        }

                        Row(Modifier, Arrangement.spacedBy(12.px), Alignment.CenterVertically) {
                            if (member.role == Role.OWNER) {
                                Box(Modifier.padding(8.px)) {
                                    Text("Owner")
                                }
                            } else {
                                Dropdown(
                                    selected = member.role.name,
                                    options = Role.entries.filter { it != Role.OWNER }.map { it.name },
                                    enabled = currentRole == Role.OWNER || currentRole == Role.ADMIN,
                                    onOptionSelected = { newRoleName ->
                                        scope.launch {
                                            handle(updateRole(board.id, member.user.id, Role.valueOf(newRoleName))) {
                                                window.location.reload()
                                            }
                                        }
                                    },
                                )

                                if (currentRole == Role.OWNER || currentRole == Role.ADMIN) {
                                    MsPersonRemove(
                                        Modifier
                                            .color(Color.error)
                                            .cursor(Cursor.Pointer)
                                            .fontSize(20.px)
                                            .onClick {
                                                if (window.confirm("Are you sure you want to kick ${member.user.username}?")) {
                                                    scope.launch {
                                                        handle(kick(board.id, member.user.id)) {
                                                            window.location.reload()
                                                        }
                                                    }
                                                }
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (currentRole == Role.OWNER || currentRole == Role.ADMIN) {
                Request(board.id, request = { getBoardInvitations(board.id) }) { invitations ->
                    if (invitations.isNotEmpty()) {
                        HorizontalDivider(Modifier.fillMaxWidth())
                        H3 { Text("Pending Invitations") }
                        Column(Modifier.fillMaxWidth().gap(12.px)) {
                            invitations.forEach { invitation ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.px)
                                        .backgroundColor(Color.surfaceContainer)
                                        .borderRadius(12.px),
                                    Arrangement.SpaceBetween,
                                    Alignment.CenterVertically,
                                ) {
                                    Row(Modifier, Arrangement.spacedBy(12.px), Alignment.CenterVertically) {
                                        Image(
                                            invitation.receiver.profilePicture,
                                            "${invitation.receiver.username}'s profile picture",
                                            Modifier.size(32.px).clip(Circle()),
                                        )
                                        Column {
                                            Text(invitation.receiver.username)
                                            P(Modifier.opacity(0.6).fontSize(12.px).toAttrs()) {
                                                Text("Invited as ${invitation.role.name.lowercase()}")
                                            }
                                        }
                                    }

                                    MsPersonRemove(
                                        Modifier
                                            .color(Color.error)
                                            .cursor(Cursor.Pointer)
                                            .fontSize(20.px)
                                            .onClick {
                                                scope.launch {
                                                    handle(declineInvitation(invitation.id)) {
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
            }

            if (currentRole == Role.OWNER) {
                HorizontalDivider(Modifier.fillMaxWidth())

                Column(Modifier.fillMaxWidth().gap(12.px)) {
                    H3 { Text("Transfer Ownership") }
                    P { Text("Promote another member to Owner. You will become an Admin.") }
                    Button(
                        { isTransferSearchOpen = true },
                        Modifier.backgroundColor(Color.tertiary),
                    ) {
                        Text("Transfer Ownership")
                    }
                }
            }

            HorizontalDivider(Modifier.fillMaxWidth())

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (currentRole == Role.OWNER) {
                    Button(
                        {
                            if (window.confirm("Are you sure you want to delete this board? This action cannot be undone.")) {
                                scope.launch {
                                    handle(deleteBoard(board.id)) {
                                        ctx.router.navigateTo("/boards/drive")
                                    }
                                }
                            }
                        },
                        Modifier.backgroundColor(Color.error),
                    ) {
                        Text("Delete Board")
                    }
                } else {
                    Box {} // Empty box to keep "Save Changes" on the right if OWNER check fails
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.px)) {
                    Button({ onBack() }) {
                        Text("Cancel")
                    }
                    Button(
                        {
                            scope.launch {
                                handle(updateBoard(board.id, BoardDetailsModel(title, description, visibility))) {
                                    window.location.reload()
                                }
                            }
                        },
                        enabled = hasChanges,
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    if (isUserSearchOpen) {
        UserSearchOverlay(
            onClose = { isUserSearchOpen = false },
            onSubmit = { userId, role ->
                invite(board.id, userId, role).map {}
            },
        )
    }

    if (isTransferSearchOpen) {
        BoardMemberSearchOverlay(
            members = board.memberships.map { it.user }.filter { it.id != board.owner.id },
            alreadyAssigned = emptyList(),
            onClose = { isTransferSearchOpen = false },
            onSubmit = { userId ->
                if (window.confirm("Are you sure you want to transfer ownership? This cannot be undone.")) {
                    scope.launch {
                        handle(updateRole(board.id, userId, Role.OWNER)) {
                            window.location.reload()
                        }
                    }
                }
            },
        )
    }
}
