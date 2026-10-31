package de.mm20.launcher2.ui.theme.typography.fontfamily

import androidx.compose.ui.text.font.FontFamily
import de.mm20.launcher2.ui.R

/**
 * Variable fonts bundled by the Blackfyre fork (built by `mise run fonts`), keyed by the
 * name stored in [de.mm20.launcher2.themes.typography.FontFamily.Bundled]. Weights are set
 * through the `wght` axis, as for [GoogleSansFlex].
 */
private val bundledFonts = mapOf(
    "archivo" to R.font.archivo,
    "jetbrains_mono" to R.font.jetbrains_mono,
    "bf_mono_caps" to R.font.bf_mono_caps,
)

fun bundledFontFamily(name: String, settings: Map<String, Float>): FontFamily {
    val resId = bundledFonts[name] ?: return FontFamily.Default
    return createVariableFontFamily(resId, settings)
}
