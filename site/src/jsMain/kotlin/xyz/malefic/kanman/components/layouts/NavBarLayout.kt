package xyz.malefic.kanman.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.borderRight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.common.DisabledStyle
import com.varabyte.kobweb.silk.style.toAttrs
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Layout
@Composable
fun NavBarLayout(
    ctx: PageContext,
    content: @Composable () -> Unit,
) {
    val currentRoute = ctx.route.path

    Row(Modifier.fillMaxSize().height(100.vh)) {
        Surface(Modifier.fillMaxHeight().borderRight(1.px, LineStyle.Solid)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(0.px, 20.px),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                H1 {
                    Text("KanMan")
                }

                Column(verticalArrangement = Arrangement.spacedBy(24.px), horizontalAlignment = Alignment.CenterHorizontally) {
                    Pages.entries.forEach { page ->
                        if (page.isPage(currentRoute)) {
                            Span(DisabledStyle.toAttrs()) {
                                Text(page.value)
                            }
                        } else {
                            Link(page.route) {
                                Text(page.value)
                            }
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
    ABOUT("About", "/about"),
    ;

    fun isPage(route: String): Boolean = route.trimEnd('/') == this@Pages.route.trimEnd('/')
}
