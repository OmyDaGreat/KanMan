package xyz.malefic.kanman.styles

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase

val SCRIPT_FONT = listOf("Lavishly Yours", "cursive")
val DISPLAY_FONT = listOf("Cormorant Garamond", "serif")
val BODY_FONT = listOf("Lora", "serif")

@InitSilk
fun initTypography(ctx: InitSilkContext) {
    ctx.stylesheet.registerStyleBase("h1, .display-large") {
        Modifier.fontFamily(SCRIPT_FONT).fontWeight(400)
    }
    ctx.stylesheet.registerStyleBase("h2, h3, .headline-medium, .title-large") {
        Modifier.fontFamily(DISPLAY_FONT).fontWeight(600)
    }
    ctx.stylesheet.registerStyleBase("body, p, button, .label-medium") {
        Modifier.fontFamily(BODY_FONT).fontWeight(400)
    }
}
