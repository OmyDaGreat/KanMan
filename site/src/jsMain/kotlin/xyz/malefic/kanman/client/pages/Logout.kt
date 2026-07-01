package xyz.malefic.kanman.client.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.util.AuthSession
import xyz.malefic.kanman.client.components.Spinner
import kotlin.time.Duration.Companion.seconds

@Page
@Composable
fun Logout(ctx: PageContext) =
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        var signedOut by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            AuthSession.signout()
            signedOut = true
            delay(5.seconds)
            ctx.router.navigateTo("/")
        }
        if (!signedOut) {
            Spinner()
            return@Box
        }
        P { Text("Successfully Logged Out") }
    }
