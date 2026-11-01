package de.mm20.launcher2.ui.component.blackfyre

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Fork (blackfyre-components): a keycap's colours: its [face], the [content] on it, the [edge]
 * below it and the [highlight] on its top edge (null for none).
 */
@Immutable
data class KeyPalette(val face: Color, val content: Color, val edge: Color, val highlight: Color?)

/**
 * Keycap palettes from the design system (tokens.json version 97, the Buttons component, and the
 * site's templates for the Dark values of the light key).
 */
object KeyPalettes {
    /** The orange key: `bf-accent`, `bf-on-accent`, `0 2px 0 bf-accent-edge, inset 0 1px 0 rgba(255,255,255,.25)`. */
    val Primary = KeyPalette(
        face = Color(0xFFE2551D),
        content = Color(0xFF1B1B19),
        edge = Color(0xFFA93A0F),
        highlight = Color.White.copy(alpha = 0.25f),
    )

    /** The light key in Paper: `bf-paper`, `bf-ink`, edge `bf-line-strong`, highlight `bf-highlight`. */
    val SecondaryPaper = KeyPalette(
        face = Color(0xFFE6E4DF),
        content = Color(0xFF1B1B19),
        edge = Color(0xFFBFBBB3),
        highlight = Color(0xFFFFFFFF),
    )

    /** The light key in Dark: the same tokens' Dark values. */
    val SecondaryDark = KeyPalette(
        face = Color(0xFF1B1B19),
        content = Color(0xFFF3F2EE),
        edge = Color(0xFF0E0E0D),
        highlight = Color(0xFF33332F),
    )

    /** A key held down in Paper: `bf-pressed` with `bf-on-pressed`; no edge shows. */
    val PressedPaper = KeyPalette(
        face = Color(0xFF1B1B19),
        content = Color(0xFFF3F2EE),
        edge = Color(0xFF1B1B19),
        highlight = null,
    )

    /** A key held down in Dark. */
    val PressedDark = KeyPalette(
        face = Color(0xFF3A3935),
        content = Color(0xFFF3F2EE),
        edge = Color(0xFF3A3935),
        highlight = null,
    )

    fun secondary(dark: Boolean) = if (dark) SecondaryDark else SecondaryPaper
    fun pressed(dark: Boolean) = if (dark) PressedDark else PressedPaper

    /** A key in a call site's own colours, with an edge of [container] darkened by [EdgeDarken]. */
    fun custom(container: Color, content: Color) = KeyPalette(
        face = container,
        content = content,
        edge = darken(container),
        highlight = null,
    )
}

/** How far towards black a custom key's edge is. */
const val EdgeDarken = 0.3f

fun darken(color: Color, fraction: Float = EdgeDarken): Color =
    lerp(color, Color.Black.copy(alpha = color.alpha), fraction)
