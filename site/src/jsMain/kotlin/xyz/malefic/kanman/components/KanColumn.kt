package xyz.malefic.kanman.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text

@Composable
fun KanColumn(title: String) =
    Column(
        Modifier.fillMaxHeight().width(20.vw).border(2.px, LineStyle.Solid, Color.black),
        Arrangement.Top,
        Alignment.CenterHorizontally,
    ) {
        H2 {
            Text(title)
        }
        HorizontalDivider(Modifier.border(1.px, LineStyle.Solid, Color.black))
    }
