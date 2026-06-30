package xyz.malefic.kanman.client.styles

import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.functions.blur
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backdropFilter
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderLeft
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.borderTop
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.letterSpacing
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textDecorationLine
import com.varabyte.kobweb.compose.ui.modifiers.transform
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.silk.components.disclosure.TabsTabStyle
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.CheckboxStyle
import com.varabyte.kobweb.silk.components.forms.InputStyle
import com.varabyte.kobweb.silk.components.forms.SwitchStyle
import com.varabyte.kobweb.silk.components.graphics.ImageStyle
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.components.layout.SurfaceStyle
import com.varabyte.kobweb.silk.components.layout.VerticalDividerStyle
import com.varabyte.kobweb.silk.components.navigation.LinkStyle
import com.varabyte.kobweb.silk.components.overlay.OverlayStyle
import com.varabyte.kobweb.silk.components.text.SpanTextStyle
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.vars.color.BorderColorVar
import com.varabyte.kobweb.silk.theme.modifyStyle
import org.jetbrains.compose.web.css.AnimationTimingFunction
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s

@InitSilk
fun initStyles(ctx: InitSilkContext) {
    ctx.theme.modifyStyle(InputStyle) {
        base {
            Modifier
                .borderRadius(16.px)
                .padding(leftRight = 16.px, topBottom = 12.px)
                .border(1.px, LineStyle.Solid, BorderColorVar.value())
                .transition {
                    property("all")
                    duration(0.2.s)
                    timingFunction(AnimationTimingFunction.EaseInOut)
                }
        }
    }

    ctx.theme.modifyStyle(ButtonStyle) {
        base {
            Modifier
                .letterSpacing(0.02.em)
                .borderRadius(16.px)
                .padding(topBottom = 12.px, leftRight = 28.px)
                .transition {
                    property("all")
                    duration(0.25.s)
                    timingFunction(AnimationTimingFunction.EaseInOut)
                }
        }
        hover {
            Modifier.transform { scale(1.02) }
        }
    }

    ctx.theme.modifyStyle(SwitchStyle) {
        base {
            Modifier.borderRadius(16.px)
        }
    }

    ctx.theme.modifyStyle(SurfaceStyle) {
        base {
            Modifier
                .transition {
                    property("background-color")
                    duration(0.3.s)
                    timingFunction(AnimationTimingFunction.Ease)
                }
        }
    }

    ctx.theme.modifyStyle(VerticalDividerStyle) {
        base {
            Modifier
                .borderLeft(1.px, LineStyle.Solid, BorderColorVar.value())
                .opacity(0.35)
        }
    }

    ctx.theme.modifyStyle(HorizontalDividerStyle) {
        base {
            Modifier
                .borderTop(1.px, LineStyle.Solid, BorderColorVar.value())
                .opacity(0.35)
        }
    }

    ctx.theme.modifyStyle(LinkStyle) {
        base {
            Modifier
                .color(primaryLight.color)
                .textDecorationLine(TextDecorationLine.None)
                .transition {
                    property("color")
                    duration(0.2.s)
                    timingFunction(AnimationTimingFunction.EaseInOut)
                }
        }
        hover {
            Modifier
                .color(primaryLight.color.darkened(0.15f))
                .textDecorationLine(TextDecorationLine.Underline)
        }
    }

    ctx.theme.modifyStyle(SpanTextStyle) {
        base {
            Modifier
                .lineHeight(1.65)
                .letterSpacing(0.01.em)
        }
    }

    ctx.theme.modifyStyle(OverlayStyle) {
        base {
            Modifier
                .backdropFilter(blur(8.px))
                .transition(Transition.all(0.3.s, AnimationTimingFunction.EaseInOut))
        }
    }

    ctx.theme.modifyStyle(CheckboxStyle) {
        base {
            Modifier
                .borderRadius(8.px)
                .border(1.px, LineStyle.Solid, BorderColorVar.value())
                .transition(Transition.all(0.2.s))
        }
    }

    ctx.theme.modifyStyle(TabsTabStyle) {
        base {
            Modifier
                .fontFamily(HEADER_FONT)
                .fontWeight(500)
                .letterSpacing(0.02.em)
                .transition(Transition.all(0.2.s))
        }
    }

    ctx.theme.modifyStyle(ImageStyle) {
        base {
            Modifier
                .borderRadius(16.px)
                .transition(Transition.all(0.2.s))
        }
    }
}
