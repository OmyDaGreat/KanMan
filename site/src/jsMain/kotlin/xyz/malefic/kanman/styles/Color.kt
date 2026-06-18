package xyz.malefic.kanman.styles

import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.rgba
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.button
import com.varabyte.kobweb.silk.theme.colors.palette.callout
import com.varabyte.kobweb.silk.theme.colors.palette.checkbox
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.focusOutline
import com.varabyte.kobweb.silk.theme.colors.palette.input
import com.varabyte.kobweb.silk.theme.colors.palette.overlay
import com.varabyte.kobweb.silk.theme.colors.palette.placeholder
import com.varabyte.kobweb.silk.theme.colors.palette.switch
import com.varabyte.kobweb.silk.theme.colors.palette.tab
import com.varabyte.kobweb.silk.theme.colors.palette.tooltip
import xyz.malefic.kutint.RGB
import xyz.malefic.kutint.parseHex

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

val primaryLightMediumContrast = parseHex("#303A00")
val onPrimaryLightMediumContrast = parseHex("#FFFFFF")
val primaryContainerLightMediumContrast = parseHex("#66732E")
val onPrimaryContainerLightMediumContrast = parseHex("#FFFFFF")
val secondaryLightMediumContrast = parseHex("#343820")
val onSecondaryLightMediumContrast = parseHex("#FFFFFF")
val secondaryContainerLightMediumContrast = parseHex("#6B7053")
val onSecondaryContainerLightMediumContrast = parseHex("#FFFFFF")
val tertiaryLightMediumContrast = parseHex("#0D3D35")
val onTertiaryLightMediumContrast = parseHex("#FFFFFF")
val tertiaryContainerLightMediumContrast = parseHex("#49756B")
val onTertiaryContainerLightMediumContrast = parseHex("#FFFFFF")
val errorLightMediumContrast = parseHex("#740006")
val onErrorLightMediumContrast = parseHex("#FFFFFF")
val errorContainerLightMediumContrast = parseHex("#CF2C27")
val onErrorContainerLightMediumContrast = parseHex("#FFFFFF")
val backgroundLightMediumContrast = parseHex("#FBFAED")
val onBackgroundLightMediumContrast = parseHex("#1B1C15")
val surfaceLightMediumContrast = parseHex("#FBFAED")
val onSurfaceLightMediumContrast = parseHex("#11120B")
val surfaceVariantLightMediumContrast = parseHex("#E3E4D3")
val onSurfaceVariantLightMediumContrast = parseHex("#35372C")
val outlineLightMediumContrast = parseHex("#525347")
val outlineVariantLightMediumContrast = parseHex("#6D6E61")
val scrimLightMediumContrast = parseHex("#000000")
val inverseSurfaceLightMediumContrast = parseHex("#303129")
val inverseOnSurfaceLightMediumContrast = parseHex("#F2F1E5")
val inversePrimaryLightMediumContrast = parseHex("#BFCE7F")
val surfaceDimLightMediumContrast = parseHex("#C8C7BB")
val surfaceBrightLightMediumContrast = parseHex("#FBFAED")
val surfaceContainerLowestLightMediumContrast = parseHex("#FFFFFF")
val surfaceContainerLowLightMediumContrast = parseHex("#F5F4E7")
val surfaceContainerLightMediumContrast = parseHex("#EAE9DC")
val surfaceContainerHighLightMediumContrast = parseHex("#DEDDD1")
val surfaceContainerHighestLightMediumContrast = parseHex("#D3D2C6")

val primaryLightHighContrast = parseHex("#273000")
val onPrimaryLightHighContrast = parseHex("#FFFFFF")
val primaryContainerLightHighContrast = parseHex("#424E0C")
val onPrimaryContainerLightHighContrast = parseHex("#FFFFFF")
val secondaryLightHighContrast = parseHex("#2A2E17")
val onSecondaryLightHighContrast = parseHex("#FFFFFF")
val secondaryContainerLightHighContrast = parseHex("#474C32")
val onSecondaryContainerLightHighContrast = parseHex("#FFFFFF")
val tertiaryLightHighContrast = parseHex("#00332B")
val onTertiaryLightHighContrast = parseHex("#FFFFFF")
val tertiaryContainerLightHighContrast = parseHex("#245148")
val onTertiaryContainerLightHighContrast = parseHex("#FFFFFF")
val errorLightHighContrast = parseHex("#600004")
val onErrorLightHighContrast = parseHex("#FFFFFF")
val errorContainerLightHighContrast = parseHex("#98000A")
val onErrorContainerLightHighContrast = parseHex("#FFFFFF")
val backgroundLightHighContrast = parseHex("#FBFAED")
val onBackgroundLightHighContrast = parseHex("#1B1C15")
val surfaceLightHighContrast = parseHex("#FBFAED")
val onSurfaceLightHighContrast = parseHex("#000000")
val surfaceVariantLightHighContrast = parseHex("#E3E4D3")
val onSurfaceVariantLightHighContrast = parseHex("#000000")
val outlineLightHighContrast = parseHex("#2B2D22")
val outlineVariantLightHighContrast = parseHex("#494A3E")
val scrimLightHighContrast = parseHex("#000000")
val inverseSurfaceLightHighContrast = parseHex("#303129")
val inverseOnSurfaceLightHighContrast = parseHex("#FFFFFF")
val inversePrimaryLightHighContrast = parseHex("#BFCE7F")
val surfaceDimLightHighContrast = parseHex("#BAB9AE")
val surfaceBrightLightHighContrast = parseHex("#FBFAED")
val surfaceContainerLowestLightHighContrast = parseHex("#FFFFFF")
val surfaceContainerLowLightHighContrast = parseHex("#F2F1E5")
val surfaceContainerLightHighContrast = parseHex("#E4E3D7")
val surfaceContainerHighLightHighContrast = parseHex("#D6D5C9")
val surfaceContainerHighestLightHighContrast = parseHex("#C8C7BB")

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

val primaryDarkMediumContrast = parseHex("#D5E492")
val onPrimaryDarkMediumContrast = parseHex("#212900")
val primaryContainerDarkMediumContrast = parseHex("#89974E")
val onPrimaryContainerDarkMediumContrast = parseHex("#000000")
val secondaryDarkMediumContrast = parseHex("#DBDFBD")
val onSecondaryDarkMediumContrast = parseHex("#232811")
val secondaryContainerDarkMediumContrast = parseHex("#8F9475")
val onSecondaryContainerDarkMediumContrast = parseHex("#000000")
val tertiaryDarkMediumContrast = parseHex("#B7E6DA")
val onTertiaryDarkMediumContrast = parseHex("#002C25")
val tertiaryContainerDarkMediumContrast = parseHex("#6C998F")
val onTertiaryContainerDarkMediumContrast = parseHex("#000000")
val errorDarkMediumContrast = parseHex("#FFD2CC")
val onErrorDarkMediumContrast = parseHex("#540003")
val errorContainerDarkMediumContrast = parseHex("#FF5449")
val onErrorContainerDarkMediumContrast = parseHex("#000000")
val backgroundDarkMediumContrast = parseHex("#13140D")
val onBackgroundDarkMediumContrast = parseHex("#E4E3D7")
val surfaceDarkMediumContrast = parseHex("#13140D")
val onSurfaceDarkMediumContrast = parseHex("#FFFFFF")
val surfaceVariantDarkMediumContrast = parseHex("#46483C")
val onSurfaceVariantDarkMediumContrast = parseHex("#DDDDCD")
val outlineDarkMediumContrast = parseHex("#B2B3A3")
val outlineVariantDarkMediumContrast = parseHex("#909183")
val scrimDarkMediumContrast = parseHex("#000000")
val inverseSurfaceDarkMediumContrast = parseHex("#E4E3D7")
val inverseOnSurfaceDarkMediumContrast = parseHex("#2A2B23")
val inversePrimaryDarkMediumContrast = parseHex("#414D0B")
val surfaceDimDarkMediumContrast = parseHex("#13140D")
val surfaceBrightDarkMediumContrast = parseHex("#44453C")
val surfaceContainerLowestDarkMediumContrast = parseHex("#070803")
val surfaceContainerLowDarkMediumContrast = parseHex("#1D1E17")
val surfaceContainerDarkMediumContrast = parseHex("#272921")
val surfaceContainerHighDarkMediumContrast = parseHex("#32332B")
val surfaceContainerHighestDarkMediumContrast = parseHex("#3D3E36")

val primaryDarkHighContrast = parseHex("#E8F8A4")
val onPrimaryDarkHighContrast = parseHex("#000000")
val primaryContainerDarkHighContrast = parseHex("#BBCA7B")
val onPrimaryContainerDarkHighContrast = parseHex("#090D00")
val secondaryDarkHighContrast = parseHex("#EFF3D0")
val onSecondaryDarkHighContrast = parseHex("#000000")
val secondaryContainerDarkHighContrast = parseHex("#C1C6A4")
val onSecondaryContainerDarkHighContrast = parseHex("#090D00")
val tertiaryDarkHighContrast = parseHex("#CAFAED")
val onTertiaryDarkHighContrast = parseHex("#000000")
val tertiaryContainerDarkHighContrast = parseHex("#9ECCC0")
val onTertiaryContainerDarkHighContrast = parseHex("#000E0B")
val errorDarkHighContrast = parseHex("#FFECE9")
val onErrorDarkHighContrast = parseHex("#000000")
val errorContainerDarkHighContrast = parseHex("#FFAEA4")
val onErrorContainerDarkHighContrast = parseHex("#220001")
val backgroundDarkHighContrast = parseHex("#13140D")
val onBackgroundDarkHighContrast = parseHex("#E4E3D7")
val surfaceDarkHighContrast = parseHex("#13140D")
val onSurfaceDarkHighContrast = parseHex("#FFFFFF")
val surfaceVariantDarkHighContrast = parseHex("#46483C")
val onSurfaceVariantDarkHighContrast = parseHex("#FFFFFF")
val outlineDarkHighContrast = parseHex("#F1F1E0")
val outlineVariantDarkHighContrast = parseHex("#C3C4B4")
val scrimDarkHighContrast = parseHex("#000000")
val inverseSurfaceDarkHighContrast = parseHex("#E4E3D7")
val inverseOnSurfaceDarkHighContrast = parseHex("#000000")
val inversePrimaryDarkHighContrast = parseHex("#414D0B")
val surfaceDimDarkHighContrast = parseHex("#13140D")
val surfaceBrightDarkHighContrast = parseHex("#505147")
val surfaceContainerLowestDarkHighContrast = parseHex("#000000")
val surfaceContainerLowDarkHighContrast = parseHex("#1F2019")
val surfaceContainerDarkHighContrast = parseHex("#303129")
val surfaceContainerHighDarkHighContrast = parseHex("#3B3C33")
val surfaceContainerHighestDarkHighContrast = parseHex("#47473E")

val RGB.color: Color
    get() = rgba(this.r, this.g, this.b, this.alpha)

@InitSilk
fun initColor(ctx: InitSilkContext) {
    ctx.theme.palettes.light.apply {
        background = backgroundLight.color
        color = onBackgroundLight.color
        border = outlineVariantLight.color
        focusOutline = primaryLight.color
        placeholder = onSurfaceVariantLight.color
        overlay = rgba(0, 0, 0, 0.5f)

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

        input.set(
            hoveredBorder = primaryLight.color,
            invalidBorder = errorLight.color,
            filled = surfaceVariantLight.color,
            filledHover = surfaceVariantLight.color.darkened(0.05f),
            filledFocus = primaryLight.color,
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

        callout.set(
            caution = errorLight.color,
            important = primaryLight.color,
            note = secondaryLight.color,
            question = tertiaryLight.color,
            quote = outlineLight.color,
            tip = tertiaryLight.color,
            warning = errorLight.color,
        )
    }

    ctx.theme.palettes.dark.apply {
        background = backgroundDark.color
        color = onBackgroundDark.color
        border = outlineVariantDark.color
        focusOutline = primaryDark.color
        placeholder = onSurfaceVariantDark.color
        overlay = rgba(0, 0, 0, 0.5f)

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

        input.set(
            hoveredBorder = primaryDark.color,
            invalidBorder = errorDark.color,
            filled = surfaceVariantDark.color,
            filledHover = surfaceVariantDark.color.darkened(0.05f),
            filledFocus = primaryDark.color,
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

        callout.set(
            caution = errorDark.color,
            important = primaryDark.color,
            note = secondaryDark.color,
            question = tertiaryDark.color,
            quote = outlineDark.color,
            tip = tertiaryDark.color,
            warning = errorDark.color,
        )
    }
}
