package xyz.malefic.kanman.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.Surface
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.styles.Colors

@Composable
fun KanColumn(column: String) =
    Column(
        Modifier
            .fillMaxHeight()
            .width(20.vw)
            .backgroundColor(Colors.primaryContainer)
            .padding(18.px)
            .borderRadius(18.px)
            .overflow(Overflow.Scroll),
        Arrangement.Top,
    ) {
        H2 {
            Text(column)
        }
        val stickyColor = listOf(Colors.tertiary, Colors.error, Colors.primary, Colors.secondary)
        Surface(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1)
                .backgroundColor(stickyColor.random())
                .padding(18.px),
        ) {
            P(Modifier.color(Colors.onTertiary).overflow(Overflow.Scroll).toAttrs()) {
                Text(
                    "this is content the most content more content than any other content no content does content like this content does content",
                )
            }
        }
    }
