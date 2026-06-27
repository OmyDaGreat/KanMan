package xyz.malefic.kanman.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.styles.Color

@Composable
fun KanColumn(column: String) =
    Column(
        Modifier
            .fillMaxHeight()
            .width(20.vw)
            .backgroundColor(Color.primaryContainer)
            .padding(18.px)
            .borderRadius(18.px)
            .overflow(Overflow.Scroll),
        Arrangement.Top,
    ) {
        H2 {
            Text(column)
        }
        val stickyColor = listOf(Color.tertiary, Color.error, Color.primary, Color.secondary)
        val selectedColor = stickyColor.random()
        StickyNote(selectedColor, "this is content of sticky note")
    }
