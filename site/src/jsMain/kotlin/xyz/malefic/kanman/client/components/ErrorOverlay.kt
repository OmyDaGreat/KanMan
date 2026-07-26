package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.browser.uri.encodeURIComponent
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsError
import com.varabyte.kobweb.silk.components.overlay.Overlay
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.api.util.GlobalErrorState
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.Issue

@Composable
fun ErrorOverlay() {
    val issue = GlobalErrorState.currentIssue ?: return
    val ctx = rememberPageContext()

    LaunchedEffect(issue) {
        if (issue is Issue.Auth) {
            GlobalErrorState.clear()
            if (ctx.route.path != "/login") {
                ctx.router.navigateTo("/login?redirect=${encodeURIComponent(ctx.route.path)}")
            }
        }
    }

    if (issue is Issue.Auth) return

    Overlay(
        Modifier.backgroundColor(Color.overlay),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .backgroundColor(Color.surfaceContainerHigh)
                .padding(24.px)
                .borderRadius(24.px)
                .border(1.px, LineStyle.Solid, Color.error)
                .width(450.px),
            Arrangement.spacedBy(16.px),
            Alignment.CenterHorizontally,
        ) {
            MsError(Modifier.fontSize(48.px).color(Color.error))

            H2 { Text("An unexpected error occurred") }

            P(Modifier.textAlign(TextAlign.Center).toAttrs()) {
                Text("Please report this to the site owner.")
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.surfaceContainer)
                    .padding(12.px)
                    .borderRadius(12.px),
                Arrangement.spacedBy(8.px),
            ) {
                P(Modifier.fontSize(14.px).opacity(0.7).toAttrs()) {
                    Text("Technical details:")
                }
                P(Modifier.fontSize(14.px).toAttrs()) {
                    Text(issue.message)
                }
            }

            Button({ GlobalErrorState.clear() }) {
                Text("Dismiss")
            }
        }
    }
}
