package xyz.malefic.kanman.client.styles

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.rgba
import com.varabyte.kobweb.compose.ui.modifiers.setVariable
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.navigation.LinkVars
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.vars.color.BackgroundColorVar
import com.varabyte.kobweb.silk.style.vars.color.BorderColorVar
import com.varabyte.kobweb.silk.style.vars.color.ColorVar
import com.varabyte.kobweb.silk.style.vars.color.FocusOutlineColorVar
import com.varabyte.kobweb.silk.style.vars.color.PlaceholderColorVar
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.cssClass
import com.varabyte.kobweb.silk.theme.colors.loadFromLocalStorage
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.button
import com.varabyte.kobweb.silk.theme.colors.palette.checkbox
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.focusOutline
import com.varabyte.kobweb.silk.theme.colors.palette.input
import com.varabyte.kobweb.silk.theme.colors.palette.link
import com.varabyte.kobweb.silk.theme.colors.palette.overlay
import com.varabyte.kobweb.silk.theme.colors.palette.placeholder
import com.varabyte.kobweb.silk.theme.colors.palette.switch
import com.varabyte.kobweb.silk.theme.colors.palette.tab
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobweb.silk.theme.colors.palette.tooltip
import com.varabyte.kobweb.silk.theme.colors.systemPreference
import xyz.malefic.kutint.RGB
import xyz.malefic.kutint.parseHex

object Color {
    @Composable
    fun colorModeAware(
        light: RGB,
        dark: RGB,
    ) = if (ColorMode.current.isLight) light else dark

    val primary
        @Composable
        get() = colorModeAware(primaryLight, primaryDark)

    val onPrimary
        @Composable
        get() = colorModeAware(onPrimaryLight, onPrimaryDark)

    val primaryContainer
        @Composable
        get() = colorModeAware(primaryContainerLight, primaryContainerDark)

    val onPrimaryContainer
        @Composable
        get() = colorModeAware(onPrimaryContainerLight, onPrimaryContainerDark)

    val secondary
        @Composable
        get() = colorModeAware(secondaryLight, secondaryDark)

    val onSecondary
        @Composable
        get() = colorModeAware(onSecondaryLight, onSecondaryDark)

    val secondaryContainer
        @Composable
        get() = colorModeAware(secondaryContainerLight, secondaryContainerDark)

    val onSecondaryContainer
        @Composable
        get() = colorModeAware(onSecondaryContainerLight, onSecondaryContainerDark)

    val tertiary
        @Composable
        get() = colorModeAware(tertiaryLight, tertiaryDark)

    val onTertiary
        @Composable
        get() = colorModeAware(onTertiaryLight, onTertiaryDark)

    val tertiaryContainer
        @Composable
        get() = colorModeAware(tertiaryContainerLight, tertiaryContainerDark)

    val onTertiaryContainer
        @Composable
        get() = colorModeAware(onTertiaryContainerLight, onTertiaryContainerDark)

    val error
        @Composable
        get() = colorModeAware(errorLight, errorDark)

    val onError
        @Composable
        get() = colorModeAware(onErrorLight, onErrorDark)

    val errorContainer
        @Composable
        get() = colorModeAware(errorContainerLight, errorContainerDark)

    val onErrorContainer
        @Composable
        get() = colorModeAware(onErrorContainerLight, onErrorContainerDark)

    val background
        @Composable
        get() = colorModeAware(backgroundLight, backgroundDark)

    val onBackground
        @Composable
        get() = colorModeAware(onBackgroundLight, onBackgroundDark)

    val surface
        @Composable
        get() = colorModeAware(surfaceLight, surfaceDark)

    val onSurface
        @Composable
        get() = colorModeAware(onSurfaceLight, onSurfaceDark)

    val surfaceVariant
        @Composable
        get() = colorModeAware(surfaceVariantLight, surfaceVariantDark)

    val onSurfaceVariant
        @Composable
        get() = colorModeAware(onSurfaceVariantLight, onSurfaceVariantDark)

    val outline
        @Composable
        get() = colorModeAware(outlineLight, outlineDark)

    val outlineVariant
        @Composable
        get() = colorModeAware(outlineVariantLight, outlineVariantDark)

    val scrim
        @Composable
        get() = colorModeAware(scrimLight, scrimDark)

    val inverseSurface
        @Composable
        get() = colorModeAware(inverseSurfaceLight, inverseSurfaceDark)

    val inverseOnSurface
        @Composable
        get() = colorModeAware(inverseOnSurfaceLight, inverseOnSurfaceDark)

    val inversePrimary
        @Composable
        get() = colorModeAware(inversePrimaryLight, inversePrimaryDark)

    val surfaceDim
        @Composable
        get() = colorModeAware(surfaceDimLight, surfaceDimDark)

    val surfaceBright
        @Composable
        get() = colorModeAware(surfaceBrightLight, surfaceBrightDark)

    val surfaceContainerLowest
        @Composable
        get() = colorModeAware(surfaceContainerLowestLight, surfaceContainerLowestDark)

    val surfaceContainerLow
        @Composable
        get() = colorModeAware(surfaceContainerLowLight, surfaceContainerLowDark)

    val surfaceContainer
        @Composable
        get() = colorModeAware(surfaceContainerLight, surfaceContainerDark)

    val surfaceContainerHigh
        @Composable
        get() = colorModeAware(surfaceContainerHighLight, surfaceContainerHighDark)

    val surfaceContainerHighest
        @Composable
        get() = colorModeAware(surfaceContainerHighestLight, surfaceContainerHighestDark)
}

val primaryLight = parseHex("#576421")
val onPrimaryLight = parseHex("#FFFFFF")
val primaryContainerLight = parseHex("#DBEA98")
val onPrimaryContainerLight = parseHex("#404C09")
val secondaryLight = parseHex("#5C6146")
val onSecondaryLight = parseHex("#FFFFFF")
val secondaryContainerLight = parseHex("#E1E6C3")
val onSecondaryContainerLight = parseHex("#444930")
val tertiaryLight = parseHex("#3A665D")
val onTertiaryLight = parseHex("#FFFFFF")
val tertiaryContainerLight = parseHex("#BDECE0")
val onTertiaryContainerLight = parseHex("#214E45")
val errorLight = parseHex("#BA1A1A")
val onErrorLight = parseHex("#FFFFFF")
val errorContainerLight = parseHex("#FFDAD6")
val onErrorContainerLight = parseHex("#93000A")
val backgroundLight = parseHex("#FBFAED")
val onBackgroundLight = parseHex("#1B1C15")
val surfaceLight = parseHex("#FBFAED")
val onSurfaceLight = parseHex("#1B1C15")
val surfaceVariantLight = parseHex("#E3E4D3")
val onSurfaceVariantLight = parseHex("#46483C")
val outlineLight = parseHex("#77786A")
val outlineVariantLight = parseHex("#C7C8B7")
val scrimLight = parseHex("#000000")
val inverseSurfaceLight = parseHex("#303129")
val inverseOnSurfaceLight = parseHex("#F2F1E5")
val inversePrimaryLight = parseHex("#BFCE7F")
val surfaceDimLight = parseHex("#DBDBCE")
val surfaceBrightLight = parseHex("#FBFAED")
val surfaceContainerLowestLight = parseHex("#FFFFFF")
val surfaceContainerLowLight = parseHex("#F5F4E7")
val surfaceContainerLight = parseHex("#EFEEE2")
val surfaceContainerHighLight = parseHex("#EAE9DC")
val surfaceContainerHighestLight = parseHex("#E4E3D7")

val primaryDark = parseHex("#BFCE7F")
val onPrimaryDark = parseHex("#2B3400")
val primaryContainerDark = parseHex("#404C09")
val onPrimaryContainerDark = parseHex("#DBEA98")
val secondaryDark = parseHex("#C5CAA8")
val onSecondaryDark = parseHex("#2E331B")
val secondaryContainerDark = parseHex("#444930")
val onSecondaryContainerDark = parseHex("#E1E6C3")
val tertiaryDark = parseHex("#A1D0C4")
val onTertiaryDark = parseHex("#04372F")
val tertiaryContainerDark = parseHex("#214E45")
val onTertiaryContainerDark = parseHex("#BDECE0")
val errorDark = parseHex("#FFB4AB")
val onErrorDark = parseHex("#690005")
val errorContainerDark = parseHex("#93000A")
val onErrorContainerDark = parseHex("#FFDAD6")
val backgroundDark = parseHex("#13140D")
val onBackgroundDark = parseHex("#E4E3D7")
val surfaceDark = parseHex("#13140D")
val onSurfaceDark = parseHex("#E4E3D7")
val surfaceVariantDark = parseHex("#46483C")
val onSurfaceVariantDark = parseHex("#C7C8B7")
val outlineDark = parseHex("#919283")
val outlineVariantDark = parseHex("#46483C")
val scrimDark = parseHex("#000000")
val inverseSurfaceDark = parseHex("#E4E3D7")
val inverseOnSurfaceDark = parseHex("#303129")
val inversePrimaryDark = parseHex("#576421")
val surfaceDimDark = parseHex("#13140D")
val surfaceBrightDark = parseHex("#393A31")
val surfaceContainerLowestDark = parseHex("#0E0F08")
val surfaceContainerLowDark = parseHex("#1B1C15")
val surfaceContainerDark = parseHex("#1F2019")
val surfaceContainerHighDark = parseHex("#2A2B23")
val surfaceContainerHighestDark = parseHex("#34352D")

val RGB.color: Color
    get() = rgba(this.r, this.g, this.b, this.alpha)

@InitSilk
fun initColor(ctx: InitSilkContext) {
    ctx.config.initialColorMode = ColorMode.loadFromLocalStorage() ?: ColorMode.systemPreference

    ctx.theme.palettes.light.apply {
        background = backgroundLight.color
        color = onBackgroundLight.color
        border = outlineVariantLight.color
        focusOutline = primaryLight.color
        placeholder = onSurfaceVariantLight.color
        overlay = rgba(0, 0, 0, 0.5f)

        input.set(
            hoveredBorder = primaryLight.color,
            invalidBorder = errorLight.color,
            filled = surfaceVariantLight.color,
            filledHover = surfaceVariantLight.color.darkened(0.05f),
            filledFocus = primaryLight.color,
        )
        button.set(
            default = primaryLight.color,
            hover = primaryLight.color.darkened(0.1f),
            focus = primaryLight.color,
            pressed = primaryLight.color.darkened(0.2f),
        )
        checkbox.set(
            background = primaryLight.color,
            hover = primaryLight.color.darkened(0.1f),
            color = onPrimaryLight.color,
        )
        switch.set(
            backgroundOn = primaryLight.color,
            backgroundOff = surfaceVariantLight.color,
            thumb = onPrimaryLight.color,
        )
        tab.set(
            color = onSurfaceVariantLight.color,
            background = surfaceLight.color,
            selectedColor = primaryLight.color,
            hover = surfaceVariantLight.color,
            pressed = surfaceVariantLight.color.darkened(0.1f),
            disabled = outlineVariantLight.color,
        )
        tooltip.set(
            background = inverseSurfaceLight.color,
            color = inverseOnSurfaceLight.color,
        )
        link.set(
            default = primaryLight.color,
            visited = primaryLight.color.darkened(0.1f),
        )
    }

    ctx.theme.palettes.dark.apply {
        background = backgroundDark.color
        color = onBackgroundDark.color
        border = outlineVariantDark.color
        focusOutline = primaryDark.color
        placeholder = onSurfaceVariantDark.color
        overlay = rgba(0, 0, 0, 0.5f)

        input.set(
            hoveredBorder = primaryDark.color,
            invalidBorder = errorDark.color,
            filled = surfaceVariantDark.color,
            filledHover = surfaceVariantDark.color.darkened(0.05f),
            filledFocus = primaryDark.color,
        )
        button.set(
            default = primaryDark.color,
            hover = primaryDark.color.darkened(0.1f),
            focus = primaryDark.color,
            pressed = primaryDark.color.darkened(0.2f),
        )
        checkbox.set(
            background = primaryDark.color,
            hover = primaryDark.color.darkened(0.1f),
            color = onPrimaryDark.color,
        )
        switch.set(
            backgroundOn = primaryDark.color,
            backgroundOff = surfaceVariantDark.color,
            thumb = onPrimaryDark.color,
        )
        tab.set(
            color = onSurfaceVariantDark.color,
            background = surfaceDark.color,
            selectedColor = primaryDark.color,
            hover = surfaceVariantDark.color,
            pressed = surfaceVariantDark.color.darkened(0.1f),
            disabled = outlineVariantDark.color,
        )
        tooltip.set(
            background = inverseSurfaceDark.color,
            color = inverseOnSurfaceDark.color,
        )
        link.set(
            default = primaryDark.color,
            visited = primaryDark.color.darkened(0.1f),
        )
    }

    ColorMode.entries.forEach { colorMode ->
        ctx.stylesheet.registerStyleBase(".${colorMode.cssClass}") {
            val palette = colorMode.toPalette()
            Modifier
                .setVariable(ButtonVars.Color, if (colorMode.isLight) onPrimaryLight.color else onPrimaryDark.color)
                .setVariable(BackgroundColorVar, palette.background)
                .setVariable(ColorVar, palette.color)
                .setVariable(BorderColorVar, palette.border)
                .setVariable(FocusOutlineColorVar, palette.focusOutline)
                .setVariable(PlaceholderColorVar, palette.placeholder)
                .setVariable(LinkVars.DefaultColor, palette.link.default)
                .setVariable(LinkVars.VisitedColor, palette.link.visited)
        }
    }
}
