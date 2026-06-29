package xyz.malefic.kanman.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.api.util.AuthSession
import xyz.malefic.kanman.api.util.Request

@Page
@Composable
fun LogoutPage(ctx: PageContext) =
    ctx.Request(request = { AuthSession.logout() }) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            P { Text("Successfully logged out") }
        }
    }
