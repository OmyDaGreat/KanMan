package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.objectFit
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.updateUser
import xyz.malefic.kanman.client.api.util.ApiState
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.UserUpdateModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun UserSettingsOverlay(
    user: UserResponseModel,
    onClose: () -> Unit,
) {
    var username by remember { mutableStateOf(user.username) }
    var profilePicture by remember { mutableStateOf(user.profilePicture) }
    var debouncedProfilePicture by remember { mutableStateOf(user.profilePicture) }
    var state by remember { mutableStateOf<ApiState<Unit>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profilePicture) {
        delay(500.milliseconds)
        debouncedProfilePicture = profilePicture
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
            H2 { Text("User Settings") }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    debouncedProfilePicture,
                    "Profile picture preview",
                    Modifier
                        .size(96.px)
                        .aspectRatio(1)
                        .clip(Circle())
                        .objectFit(ObjectFit.Cover)
                        .border(2.px, LineStyle.Solid, Color.primary),
                )
            }

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
                P(Modifier.padding(0.px).toAttrs()) { Text("Profile Picture URL") }
                TextInput(
                    profilePicture,
                    { profilePicture = it },
                    Modifier.fillMaxWidth(),
                    placeholder = "https://example.com/avatar.png",
                )
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

                            handle(updateUser(UserUpdateModel(username, profilePicture))) {
                                state = ApiState.Success(Unit)
                                onClose()
                                window.location.reload()
                            }

                            if (state is ApiState.Loading) state = null
                        }
                    },
                    enabled = username.isNotBlank() && profilePicture.isNotBlank() && state !is ApiState.Loading,
                ) {
                    Text(if (state is ApiState.Loading) "Saving..." else "Save Changes")
                }
            }
        }
    }
}
