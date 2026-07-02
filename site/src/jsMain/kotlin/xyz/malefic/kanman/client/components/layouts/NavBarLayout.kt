package xyz.malefic.kanman.client.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.common.DisabledStyle
import com.varabyte.kobweb.silk.style.toAttrs
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.api.util.AuthSession
import xyz.malefic.kanman.client.api.util.Request
import xyz.malefic.kanman.client.styles.Color

@Layout
@Composable
fun NavBarLayout(
    ctx: PageContext,
    content: @Composable () -> Unit,
) {
    val currentRoute = ctx.route.path

    Row(Modifier.fillMaxSize().height(100.vh)) {
        Surface(Modifier.fillMaxHeight().backgroundColor(Color.surfaceContainerHigh)) {
            Column(Modifier.fillMaxSize().padding(16.px), horizontalAlignment = Alignment.CenterHorizontally) {
                H1(Modifier.margin(16.px).toAttrs()) { Text("KanMan") }

                Column(verticalArrangement = Arrangement.spacedBy(24.px), horizontalAlignment = Alignment.CenterHorizontally) {
                    Pages.entries.forEach { page ->
                        if (page.hasRoute(currentRoute)) {
                            Span(DisabledStyle.toAttrs()) { Text(page.value) }
                        } else {
                            Link(page.route) { Text(page.value) }
                        }
                    }
                }

                if (AuthSession.accessToken != null) {
                    ctx.Request(Unit, request = { getUser() }) { user ->
                        Spacer()
                        Column(
                            Modifier.padding(16.px).backgroundColor(Color.secondaryContainer).borderRadius(16.px),
                            verticalArrangement = Arrangement.spacedBy(12.px),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            P { Text(user.username) }
                            Link("/logout") { Text("Log Out") }
                        }
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize().weight(1).overflow(Overflow.Auto)) {
            content()
        }
    }
}

enum class Pages(
    val value: String,
    val route: String,
) {
    INDEX("Index", "/"),
    PUBLIC("Public Boards", "/boards/public"),
    DRIVE("Your Boards", "/boards/drive"),
    ABOUT("About", "/about"),
    ;

    fun hasRoute(route: String): Boolean = route.trimEnd('/') == this@Pages.route.trimEnd('/')
}
