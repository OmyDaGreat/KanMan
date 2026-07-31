package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.silk.components.icons.ms.MsCheckBox
import com.varabyte.kobweb.silk.components.icons.ms.MsCheckBoxOutlineBlank
import com.varabyte.kobweb.silk.components.icons.ms.MsSwitchLeft
import com.varabyte.kobweb.silk.components.icons.ms.MsSwitchRight
import org.jetbrains.compose.web.css.px
import xyz.malefic.kanman.client.styles.Color

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isSwitch: Boolean = false,
    content: @Composable () -> Unit,
) {
    Row(
        modifier
            .cursor(Cursor.Pointer)
            .gap(8.px)
            .onClick { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (checked) {
            if (isSwitch) {
                MsSwitchRight(Modifier.color(Color.outline))
            } else {
                MsCheckBox(Modifier.color(Color.outline))
            }
        } else {
            if (isSwitch) {
                MsSwitchLeft(Modifier.color(Color.outline))
            } else {
                MsCheckBoxOutlineBlank(Modifier.color(Color.outline))
            }
        }
        content()
    }
}
