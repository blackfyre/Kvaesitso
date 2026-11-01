package de.mm20.launcher2.themes.blackfyre

import de.mm20.launcher2.themes.colors.Color
import de.mm20.launcher2.themes.colors.ColorScheme
import de.mm20.launcher2.themes.colors.StaticColor

import org.junit.Assert
import org.junit.Test
import kotlin.math.pow

/**
 * WCAG 2 contrast of the Blackfyre colour schemes (OpenSpec change blackfyre-theme,
 * requirement "Accent contrast rules hold").
 */
class BlackfyreColorsContrastTest {

    private val schemes = mapOf(
        "Paper" to BlackfyreLightColorScheme,
        "Dark" to BlackfyreDarkColorScheme,
    )

    /** Foreground/background pairs from the design's colour mapping table. */
    private val pairs = listOf(
        Triple("onPrimary", ColorScheme::onPrimary, ColorScheme::primary),
        Triple("onPrimaryContainer", ColorScheme::onPrimaryContainer, ColorScheme::primaryContainer),
        Triple("onSecondary", ColorScheme::onSecondary, ColorScheme::secondary),
        Triple("onSecondaryContainer", ColorScheme::onSecondaryContainer, ColorScheme::secondaryContainer),
        Triple("onTertiary", ColorScheme::onTertiary, ColorScheme::tertiary),
        Triple("onTertiaryContainer", ColorScheme::onTertiaryContainer, ColorScheme::tertiaryContainer),
        Triple("onError", ColorScheme::onError, ColorScheme::error),
        Triple("onErrorContainer", ColorScheme::onErrorContainer, ColorScheme::errorContainer),
        Triple("onBackground", ColorScheme::onBackground, ColorScheme::background),
        Triple("onSurface", ColorScheme::onSurface, ColorScheme::surface),
        Triple("onSurfaceVariant", ColorScheme::onSurfaceVariant, ColorScheme::surfaceVariant),
        Triple("inverseOnSurface", ColorScheme::inverseOnSurface, ColorScheme::inverseSurface),
        Triple("inversePrimary", ColorScheme::inversePrimary, ColorScheme::inverseSurface),
    )

    /** Surface roles that text in onSurface, onSurfaceVariant or primary is drawn on. */
    private val surfaces = listOf(
        ColorScheme::background,
        ColorScheme::surface,
        ColorScheme::surfaceDim,
        ColorScheme::surfaceBright,
        ColorScheme::surfaceContainerLowest,
        ColorScheme::surfaceContainerLow,
        ColorScheme::surfaceContainer,
        ColorScheme::surfaceContainerHigh,
        ColorScheme::surfaceContainerHighest,
    )

    @Test
    fun tablePairsReachAA() {
        val failures = schemes.flatMap { (name, scheme) ->
            pairs.mapNotNull { (label, fg, bg) ->
                val ratio = contrast(fg(scheme), bg(scheme))
                if (ratio < 4.5) "$name $label: %.2f".format(ratio) else null
            }
        }
        Assert.assertEquals(emptyList<String>(), failures)
    }

    @Test
    fun textRolesReachAAOnEverySurface() {
        val foregrounds = listOf(
            "onSurface" to ColorScheme::onSurface,
            "onSurfaceVariant" to ColorScheme::onSurfaceVariant,
            "primary" to ColorScheme::primary,
        )
        val failures = schemes.flatMap { (name, scheme) ->
            foregrounds.flatMap { (label, fg) ->
                surfaces.mapNotNull { bg ->
                    val ratio = contrast(fg(scheme), bg(scheme))
                    if (ratio < 4.5) "$name $label on ${bg.name}: %.2f".format(ratio) else null
                }
            }
        }
        Assert.assertEquals(emptyList<String>(), failures)
    }

    /**
     * Material draws switch thumbs and borders, and outlined control borders, in `outline` on
     * these roles; WCAG 2 asks 3:1 for such non-text boundaries.
     */
    @Test
    fun outlineReachesNonTextContrastOnEverySurface() {
        val failures = schemes.flatMap { (name, scheme) ->
            surfaces.mapNotNull { bg ->
                val ratio = contrast(scheme.outline, bg(scheme))
                if (ratio < 3.0) "$name outline on ${bg.name}: %.2f".format(ratio) else null
            }
        }
        Assert.assertEquals(emptyList<String>(), failures)
    }

    @Test
    fun accentIsNeverTextOnLight() {
        val accent = StaticColor(0xFFE2551D.toInt())
        val light = BlackfyreLightColorScheme
        listOf(light.primary, light.onSurface, light.onSurfaceVariant, light.secondary, light.tertiary)
            .forEach { Assert.assertNotEquals(accent, it) }
    }

    @Test
    fun everyRoleIsStatic() {
        schemes.forEach { (name, scheme) ->
            Assert.assertTrue(name, roles(scheme).all { it is StaticColor })
        }
    }

    private fun roles(scheme: ColorScheme): List<Color?> =
        pairs.flatMap { (_, fg, bg) -> listOf(fg(scheme), bg(scheme)) } +
            surfaces.map { it(scheme) } +
            listOf(scheme.outline, scheme.outlineVariant, scheme.surfaceTint, scheme.scrim)

    private fun contrast(a: Color?, b: Color?): Double {
        Assert.assertTrue("role missing", a is StaticColor && b is StaticColor)
        val la = luminance((a as StaticColor).color)
        val lb = luminance((b as StaticColor).color)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun luminance(argb: Int): Double {
        fun channel(shift: Int): Double {
            val v = ((argb shr shift) and 0xFF) / 255.0
            return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
