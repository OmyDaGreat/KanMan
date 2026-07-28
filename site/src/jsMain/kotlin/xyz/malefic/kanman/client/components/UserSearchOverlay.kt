package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.overlay.Overlay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.api.util.ApiState
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Role
import kotlin.uuid.Uuid

@Composable
fun UserSearchOverlay(
    onClose: () -> Unit,
    onSubmit: suspend (Uuid, Role) -> Either<Issue, Unit>,
) {
    var username by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.MEMBER) }
    var state by remember { mutableStateOf<ApiState<Unit>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
            H2 { Text("Invite User") }

            Column(Modifier.fillMaxWidth().padding(bottom = 8.px), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Username") }
                TextInput(
                    username,
                    { username = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "Username",
                )
            }

            Column(Modifier.fillMaxWidth().padding(bottom = 8.px), Arrangement.spacedBy(8.px)) {
                P(Modifier.padding(0.px).toAttrs()) { Text("Role") }
                Dropdown(
                    selected = role.name,
                    options = Role.entries.filter { it != Role.OWNER }.map { it.name },
                    modifier = Modifier.fillMaxWidth(),
                    onOptionSelected = { role = Role.valueOf(it) },
                )
            }

            errorMessage?.let {
                P(Modifier.color(Color.error).toAttrs()) { Text(it) }
            }

            if (state is ApiState.Error) {
                P(Modifier.color(Color.error).toAttrs()) { Text("User not found.") }
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
                        scope.launch {
                            state = ApiState.Loading
                            errorMessage = null

                            getUser(username).fold(
                                {
                                    state = ApiState.Error(it)
                                },
                                { user ->
                                    onSubmit(user.id, role).fold(
                                        { issue ->
                                            errorMessage = issue.message
                                            state = null
                                        },
                                        {
                                            state = ApiState.Success(Unit)
                                            onClose()
                                        },
                                    )
                                },
                            )
                        }
                    },
                    enabled = username.isNotBlank() && state !is ApiState.Loading,
                ) {
                    Text(if (state is ApiState.Loading) "Inviting..." else "Invite")
                }
            }
        }
    }
}
