package de.mm20.launcher2.ui.launcher.widgets.clock

import androidx.compose.ui.graphics.Color
import org.junit.Assert
import org.junit.Test

class ClockFaceColorsTest {
    // Blackfyre dark: bf-accent-on-dark, bf-accent, bf-on-accent.
    private val accentOnDark = Color(0xFFFF8A4C)
    private val accent = Color(0xFFE2551D)
    private val onAccent = Color(0xFF1B1B19)

    // Material tonal dark scheme: primary tone 80, container tone 30, on-container tone 90.
    private val tone80 = Color(0xFFFFB597)
    private val tone30 = Color(0xFF7D2D00)
    private val tone90 = Color(0xFFFFDBCD)

    @Test
    fun darkInkOnAccentIsReplacedByPrimaryWhenDark() {
        Assert.assertEquals(accentOnDark, clockFaceOnPrimaryContainer(true, accentOnDark, accent, onAccent))
    }

    @Test
    fun tonalSchemeIsKept() {
        Assert.assertEquals(tone90, clockFaceOnPrimaryContainer(true, tone80, tone30, tone90))
    }

    @Test
    fun lightLauncherIsKept() {
        Assert.assertEquals(onAccent, clockFaceOnPrimaryContainer(false, accentOnDark, accent, onAccent))
    }
}
