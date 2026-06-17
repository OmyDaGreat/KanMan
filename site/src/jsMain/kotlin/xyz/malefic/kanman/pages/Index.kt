package xyz.malefic.kanman.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import org.jetbrains.compose.web.css.px
import xyz.malefic.kanman.components.KanColumn

@Page
@Composable
fun HomePage() {
    Row(
        Modifier.fillMaxSize().padding(12.px),
        Arrangement.SpaceEvenly,
        Alignment.CenterVertically,
    ) {
        KanColumn("Backlog")
        KanColumn("Planning")
        KanColumn("In Progress")
        KanColumn("Done")
    }
}
