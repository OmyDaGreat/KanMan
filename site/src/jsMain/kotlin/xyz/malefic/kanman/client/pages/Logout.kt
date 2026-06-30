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
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.util.AuthSession
import xyz.malefic.kanman.client.components.Spinner

@Page
@Composable
fun LogoutPage() =
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        var signedOut by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            AuthSession.signout()
            signedOut = true
        }
        if (!signedOut) {
            Spinner()
            return@Box
        }
        P { Text("Successfully Logged Out") }
    }
