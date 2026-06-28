package xyz.malefic.kanman.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.silk.components.forms.TextInput
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.api.util.ApiState
import xyz.malefic.kanman.api.util.AuthSession
import xyz.malefic.kanman.styles.Color

enum class Login(
    val string: String,
) {
    LOGIN("Login"),
    SIGNUP("Sign Up"),
}

@Page
@Composable
fun LoginPage(ctx: PageContext) =
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        val scope = rememberCoroutineScope()
        var loginMode by remember { mutableStateOf(Login.LOGIN) }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loginStatus by remember { mutableStateOf<ApiState<Unit>?>(null) }

        Column(
            Modifier
                .backgroundColor(Color.surfaceContainerHigh)
                .padding(24.px)
                .borderRadius(24.px)
                .border(1.px, LineStyle.Solid, Color.outlineVariant),
            Arrangement.spacedBy(24.px),
        ) {
            H2 { Text(loginMode.string) }

            TextInput(
                username,
                { username = it },
                placeholder = "Username",
                valid = username.isNotEmpty() && loginStatus !is ApiState.Error,
            )
            TextInput(
                password,
                { password = it },
                placeholder = "Password",
                password = true,
                valid = password.isNotEmpty() && loginStatus !is ApiState.Error,
            )

            if (loginStatus is ApiState.Error) {
                P(Modifier.color(Color.error).toAttrs()) {
                    Text((loginStatus as ApiState.Error).issue.message)
                }
            }

            Button(
                {
                    scope.launch {
                        loginStatus = ApiState.Loading
                        if (loginMode == Login.LOGIN) {
                            AuthSession.login(username, password)
                        } else {
                            AuthSession.signup(username, password)
                        }.fold(
                            { issue -> loginStatus = ApiState.Error(issue) },
                            {
                                loginStatus = ApiState.Success(Unit)
                                ctx.router.navigateTo("/") // TODO: Save previous page (send as routing data?)
                            },
                        )
                    }
                },
                enabled = loginStatus !is ApiState.Loading && username.isNotEmpty() && password.isNotEmpty(),
            ) {
                Text(if (loginStatus is ApiState.Loading) "Loading..." else "Submit")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.px), verticalAlignment = Alignment.CenterVertically) {
                Switch(loginMode == Login.SIGNUP, { loginMode = if (it) Login.SIGNUP else Login.LOGIN })
                P(Modifier.padding(0.px).toAttrs()) {
                    Text("Switch to ${if (loginMode == Login.LOGIN) "Sign Up" else "Login"}")
                }
            }
        }
    }
