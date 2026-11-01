package de.mm20.launcher2.ui.theme.blackfyre

import android.view.Surface
import org.junit.Assert
import org.junit.Test

class BlackfyreLightTest {
    private val g = 9.81f
    private val eps = 1e-4f

    private fun assertLight(expected: Light, actual: Light) {
        Assert.assertEquals(expected.cx, actual.cx, eps)
        Assert.assertEquals(expected.cy, actual.cy, eps)
    }

    @Test
    fun flatFaceUpIsOverhead() {
        assertLight(Light.Overhead, lightFromGravity(0f, 0f, g, Surface.ROTATION_0))
    }

    @Test
    fun uprightPortraitCastsDown() {
        assertLight(Light(0f, 1f), lightFromGravity(0f, g, 0f, Surface.ROTATION_0))
    }

    @Test
    fun rightEdgeDownCastsRight() {
        // The right edge is down, so the upward vector's x is negative.
        Assert.assertTrue(lightFromGravity(-3f, 0f, 9f, Surface.ROTATION_0).cx > 0f)
    }

    @Test
    fun displayRotationMapsToScreenAxes() {
        // Device held with its top edge up in each rotation: shadows fall down the screen.
        assertLight(Light(0f, 1f), lightFromGravity(0f, g, 0f, Surface.ROTATION_0))
        assertLight(Light(0f, 1f), lightFromGravity(g, 0f, 0f, Surface.ROTATION_90))
        assertLight(Light(0f, 1f), lightFromGravity(0f, -g, 0f, Surface.ROTATION_180))
        assertLight(Light(0f, 1f), lightFromGravity(-g, 0f, 0f, Surface.ROTATION_270))
    }

    @Test
    fun displayRotationKeepsHorizontalDirection() {
        // The screen's right edge down: shadows move right in every rotation.
        assertLight(Light(1f, 0f), lightFromGravity(-g, 0f, 0f, Surface.ROTATION_0))
        assertLight(Light(1f, 0f), lightFromGravity(0f, g, 0f, Surface.ROTATION_90))
        assertLight(Light(1f, 0f), lightFromGravity(g, 0f, 0f, Surface.ROTATION_180))
        assertLight(Light(1f, 0f), lightFromGravity(0f, -g, 0f, Surface.ROTATION_270))
    }

    @Test
    fun valuesStayWithinRange() {
        val l = lightFromGravity(-20f, 20f, 0f, Surface.ROTATION_0)
        Assert.assertTrue(l.cx in -1f..1f && l.cy in -1f..1f)
        assertLight(Light.Overhead, lightFromGravity(0f, 0f, 0f, Surface.ROTATION_0))
    }

    @Test
    fun firstSampleIsTakenAsIs() {
        assertLight(Light(0.5f, -0.5f), smoothLight(null, Light(0.5f, -0.5f), 0L))
    }

    @Test
    fun smoothingReachesAboutTwoThirdsAfterTheLag() {
        val l = smoothLight(Light.Overhead, Light(1f, 1f), LightLagNanos)
        Assert.assertEquals(0.632f, l.cx, 0.001f)
        Assert.assertEquals(0.632f, l.cy, 0.001f)
    }

    @Test
    fun noTimeMeansNoMove() {
        assertLight(Light.Overhead, smoothLight(Light.Overhead, Light(1f, 1f), 0L))
    }
}
