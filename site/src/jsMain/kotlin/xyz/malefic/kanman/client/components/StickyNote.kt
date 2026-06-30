package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Background
import com.varabyte.kobweb.compose.css.BackgroundImage
import com.varabyte.kobweb.compose.css.BackgroundPosition
import com.varabyte.kobweb.compose.css.BackgroundRepeat
import com.varabyte.kobweb.compose.css.BackgroundSize
import com.varabyte.kobweb.compose.css.CSSPosition
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.functions.linearGradient
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.Surface
import org.jetbrains.compose.web.css.deg
import org.jetbrains.compose.web.css.div
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.client.styles.Color
import xyz.malefic.kanman.shared.data.model.StickyNoteModel
import xyz.malefic.kutint.KutintColor

@Composable
fun StickyNote(
    color: KutintColor<*>,
    stickyNote: StickyNoteModel,
) {
    val foldSize = 10.percent

    Surface(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1)
            .background(
                Background.of(
                    BackgroundImage.of(
                        linearGradient(315.deg) {
                            add(Colors.Transparent, foldSize / 2)
                            add(color, 0.percent)
                        },
                    ),
                ),
                Background.of(
                    BackgroundImage.of(
                        linearGradient(135.deg) {
                            add(color.dim(0.3f), 50.percent)
                            add(Colors.Transparent, 0.percent)
                        },
                    ),
                    BackgroundRepeat.NoRepeat,
                    BackgroundSize.of(foldSize, foldSize),
                    BackgroundPosition.of(CSSPosition(100.percent, 100.percent)),
                ),
            ).padding(18.px),
    ) {
        H3(Modifier.color(Color.onTertiary).overflow(Overflow.Scroll).toAttrs()) {
            Text(stickyNote.title)
        }
        P(Modifier.color(Color.onTertiary).overflow(Overflow.Scroll).toAttrs()) {
            Text(stickyNote.content)
        }
    }
}
