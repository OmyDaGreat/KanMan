package xyz.malefic.kanman.client.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Background
import com.varabyte.kobweb.compose.css.BackgroundImage
import com.varabyte.kobweb.compose.css.BackgroundPosition
import com.varabyte.kobweb.compose.css.BackgroundRepeat
import com.varabyte.kobweb.compose.css.BackgroundSize
import com.varabyte.kobweb.compose.css.CSSPosition
import com.varabyte.kobweb.compose.css.OverflowWrap
import com.varabyte.kobweb.compose.css.TextWrap
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.css.functions.linearGradient
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.aspectRatio
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.draggable
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onDragStart
import com.varabyte.kobweb.compose.ui.modifiers.overflowWrap
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textWrap
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.ms.MsDelete
import com.varabyte.kobweb.silk.components.icons.ms.MsEdit
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
import xyz.malefic.kutint.Kutint

@Composable
fun StickyNote(
    color: Kutint<*>,
    stickyNote: StickyNoteModel,
    canEdit: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val foldSize = 10.percent

    var modifier =
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
                            add(color.shade(0.3f), 50.percent)
                            add(Colors.Transparent, 0.percent)
                        },
                    ),
                    BackgroundRepeat.NoRepeat,
                    BackgroundSize.of(foldSize, foldSize),
                    BackgroundPosition.of(CSSPosition(100.percent, 100.percent)),
                ),
            )

    if (canEdit) {
        modifier =
            modifier
                .draggable(true)
                .onDragStart { event ->
                    event.dataTransfer?.setData("text/plain", stickyNote.id.toString())
                }
    }

    Surface(modifier.padding(18.px).margin(topBottom = 12.px)) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth()) {
                H3(
                    Modifier
                        .fillMaxWidth()
                        .color(Color.onTertiary)
                        .overflowWrap(OverflowWrap.BreakWord)
                        .toAttrs(),
                ) {
                    Text(stickyNote.title)
                }
                P(
                    Modifier
                        .fillMaxWidth()
                        .color(Color.onTertiary)
                        .whiteSpace(WhiteSpace.PreWrap)
                        .overflowWrap(OverflowWrap.BreakWord)
                        .textWrap(TextWrap.Wrap)
                        .toAttrs(),
                ) {
                    Text(stickyNote.content)
                }
            }

            if (canEdit) {
                Row(
                    Modifier.align(Alignment.BottomEnd).backgroundColor(Colors.Transparent).padding(0.px),
                    Arrangement.spacedBy(8.px),
                    Alignment.CenterVertically,
                ) {
                    Button(
                        { onEdit() },
                        Modifier.backgroundColor(Colors.Transparent).padding(0.px),
                    ) {
                        MsEdit(Modifier.color(Color.onTertiary))
                    }
                    Button(
                        { onDelete() },
                        Modifier.backgroundColor(Colors.Transparent).padding(0.px),
                    ) {
                        MsDelete(Modifier.color(Color.onTertiary))
                    }
                }
            }
        }
    }
}
