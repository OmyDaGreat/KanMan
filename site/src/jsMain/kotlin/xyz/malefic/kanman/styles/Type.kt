package xyz.malefic.kanman.styles

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.cssRem

val FANCY_FONT = listOf("Comfortaa", "cursive")
val DISPLAY_FONT = listOf("Cormorant Garamond", "serif")
val BODY_FONT = listOf("Lora", "serif")

@InitSilk
fun initTypography(ctx: InitSilkContext) {
    ctx.stylesheet.registerStyleBase("h1, .display-large") {
        Modifier
            .fontFamily(FANCY_FONT)
            .fontWeight(600)
            .fontSize(3.5.rem)
    }

    ctx.stylesheet.registerStyleBase("h2, .headline-medium") {
        Modifier
            .fontFamily(DISPLAY_FONT)
            .fontWeight(400)
            .fontSize(2.25.rem)
    }

    ctx.stylesheet.registerStyleBase("h3, .title-large") {
        Modifier
            .fontFamily(DISPLAY_FONT)
            .fontWeight(500)
            .fontSize(1.75.rem)
    }

    ctx.stylesheet.registerStyleBase("body, p, span") {
        Modifier
            .fontFamily(BODY_FONT)
            .fontWeight(400)
            .fontSize(1.0.rem)
    }

    ctx.stylesheet.registerStyleBase("button, .label-medium") {
        Modifier
            .fontFamily(BODY_FONT)
            .fontWeight(500)
            .fontSize(0.875.rem)
    }
}

val Number.rem: CSSSizeValue<CSSUnit.rem>
    get() = this.cssRem
