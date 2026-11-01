package de.mm20.launcher2.ui.component.blackfyre

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert
import org.junit.Test

class KeyPalettesTest {

    @Test
    fun primaryKeyMatchesTheTokens() {
        // bf-accent, bf-on-accent, bf-accent-edge, inset 0 1px 0 rgba(255,255,255,.25).
        Assert.assertEquals(
            KeyPalette(Color(0xFFE2551D), Color(0xFF1B1B19), Color(0xFFA93A0F), Color.White.copy(alpha = 0.25f)),
            KeyPalettes.Primary,
        )
    }

    @Test
    fun secondaryKeyFollowsTheTheme() {
        // bf-paper, bf-ink, bf-line-strong, bf-highlight: Paper and Dark values.
        Assert.assertEquals(
            KeyPalette(Color(0xFFE6E4DF), Color(0xFF1B1B19), Color(0xFFBFBBB3), Color(0xFFFFFFFF)),
            KeyPalettes.secondary(dark = false),
        )
        Assert.assertEquals(
            KeyPalette(Color(0xFF1B1B19), Color(0xFFF3F2EE), Color(0xFF0E0E0D), Color(0xFF33332F)),
            KeyPalettes.secondary(dark = true),
        )
    }

    @Test
    fun heldKeyUsesPressedTokens() {
        // bf-pressed with bf-on-pressed; no highlight.
        Assert.assertEquals(Color(0xFF1B1B19), KeyPalettes.pressed(false).face)
        Assert.assertEquals(Color(0xFF3A3935), KeyPalettes.pressed(true).face)
        Assert.assertEquals(Color(0xFFF3F2EE), KeyPalettes.pressed(true).content)
        Assert.assertNull(KeyPalettes.pressed(false).highlight)
    }

    @Test
    fun customKeyDarkensItsContainerForTheEdge() {
        val p = KeyPalettes.custom(Color(1f, 0.5f, 0f), Color.White)
        Assert.assertEquals(Color(1f, 0.5f, 0f), p.face)
        // Compose interpolates colours in Oklab, so check the direction, not exact channels.
        Assert.assertTrue(p.edge.luminance() < p.face.luminance())
        Assert.assertTrue(p.edge.luminance() > 0f)
        Assert.assertEquals(1f, p.edge.alpha, 0f)
    }

    @Test
    fun microIsTenSpTrackedOut() {
        Assert.assertEquals(10.sp, MicroMetrics.fontSize)
        Assert.assertEquals(0.12.em, MicroMetrics.letterSpacing)
    }
}
