package de.mm20.launcher2.themes.blackfyre

import de.mm20.launcher2.themes.BlackfyreThemeId
import de.mm20.launcher2.themes.typography.DefaultEmphasizedTextStyles
import de.mm20.launcher2.themes.typography.FontFamily
import de.mm20.launcher2.themes.typography.FontWeight
import de.mm20.launcher2.themes.typography.TextStyle
import de.mm20.launcher2.themes.typography.TextStyles
import de.mm20.launcher2.themes.typography.Typography

// Blackfyre typography (OpenSpec change blackfyre-theme, design decisions 3 to 5).
// Material 3 slot sizes (sp) are kept; family, weight, line height (em) and letter-spacing
// (em) come from the Blackfyre type styles in tokens.json. Label slots use "BF Mono Caps",
// the capitals cut of JetBrains Mono built by `mise run fonts`.

private fun style(font: String, size: Int, weight: Int, lineHeight: Float, letterSpacing: Float) =
    TextStyle(
        fontFamily = font,
        fontSize = size,
        fontWeight = FontWeight.Absolute(weight),
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
    )

val BlackfyreTextStyles = TextStyles(
    displayLarge = style("brand", 57, 600, 0.9f, -0.045f), // display
    displayMedium = style("brand", 45, 600, 0.95f, -0.04f), // title
    displaySmall = style("brand", 36, 600, 1.05f, -0.03f), // headline
    headlineLarge = style("brand", 32, 600, 1.15f, -0.02f), // h2
    headlineMedium = style("brand", 28, 600, 1.1f, -0.02f), // h3
    headlineSmall = style("brand", 24, 600, 1.2f, -0.01f), // h4
    titleLarge = style("brand", 22, 600, 1.2f, -0.01f), // h4
    titleMedium = style("brand", 16, 600, 1.2f, 0f), // h4
    titleSmall = style("brand", 14, 500, 1.2f, 0f), // h4 at statement weight
    bodyLarge = style("plain", 16, 400, 1.5f, 0f), // body
    bodyMedium = style("plain", 14, 400, 1.5f, 0f), // body-sm
    bodySmall = style("plain", 12, 400, 1.5f, 0f), // body-sm
    labelLarge = style("caps", 13, 400, 1.3f, 0.06f), // label, button size
    labelMedium = style("caps", 12, 400, 1.3f, 0.06f), // label
    labelSmall = style("caps", 11, 400, 1.3f, 0.1f), // kicker
)

val BlackfyreTypography = Typography(
    id = BlackfyreThemeId,
    builtIn = true,
    name = "Blackfyre",
    fonts = mapOf(
        "plain" to FontFamily.Bundled("archivo"),
        "brand" to FontFamily.Bundled("archivo"),
        "mono" to FontFamily.Bundled("jetbrains_mono"),
        "caps" to FontFamily.Bundled("bf_mono_caps"),
    ),
    styles = BlackfyreTextStyles,
    emphasizedStyles = DefaultEmphasizedTextStyles,
)
