package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import xyz.malefic.kanman.client.styles.Color

@Composable
fun Dropdown(
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onOptionSelected: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier
            .position(Position.Relative)
            .width(150.px)
            .then(
                if (!enabled) Modifier.opacity(0.5).cursor(Cursor.NotAllowed)
                else Modifier.cursor(Cursor.Pointer)
            ),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(8.px)
                .backgroundColor(Color.surfaceContainer)
                .border(1.px, LineStyle.Solid, Color.outlineVariant)
                .borderRadius(8.px)
                .onClick { if (enabled) expanded = !expanded },
        ) {
            SpanText(selected)
        }

        if (expanded && enabled) {
            Column(
                Modifier
                    .position(Position.Absolute)
                    .top(105.percent)
                    .left(0.px)
                    .fillMaxWidth()
                    .backgroundColor(Color.surfaceContainerHigh)
                    .border(1.px, LineStyle.Solid, Color.outlineVariant)
                    .borderRadius(8.px)
                    .zIndex(100),
            ) {
                options.forEach { option ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.px)
                            .onClick {
                                expanded = false
                                onOptionSelected(option)
                            },
                    ) {
                        SpanText(option)
                    }
                }
            }
        }
    }
}
