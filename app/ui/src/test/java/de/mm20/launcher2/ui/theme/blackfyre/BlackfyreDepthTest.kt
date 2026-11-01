package de.mm20.launcher2.ui.theme.blackfyre

import androidx.compose.ui.graphics.Color
import org.junit.Assert
import org.junit.Test

class BlackfyreDepthTest {

    private fun assertOffset(x: Float, y: Float, light: Light) {
        val o = panelCastOffset(light)
        Assert.assertEquals(x, o.x.value, 1e-4f)
        Assert.assertEquals(y, o.y.value, 1e-4f)
    }

    @Test
    fun castOffsetFollowsTheSiteFormula() {
        assertOffset(0f, 10f, Light.Overhead)
        assertOffset(18f, 19f, Light(1f, 1f))
        assertOffset(-18f, 1f, Light(-1f, -1f))
    }

    @Test
    fun panelColoursMatchTheTokens() {
        // bf-depth-panel (tokens.json, version 97).
        Assert.assertEquals(Color(0xFFFFFFFF), PaperPanelDepth.highlight)
        Assert.assertEquals(Color(40, 36, 30).copy(alpha = 0.08f), PaperPanelDepth.contact)
        Assert.assertEquals(Color(40, 36, 30).copy(alpha = 0.08f), PaperPanelDepth.cast)
        Assert.assertEquals(Color(0xFF33332F), DarkPanelDepth.highlight)
        Assert.assertEquals(Color.Black.copy(alpha = 0.4f), DarkPanelDepth.contact)
        Assert.assertEquals(Color.Black.copy(alpha = 0.35f), DarkPanelDepth.cast)
    }

    @Test
    fun blurRadiusMatchesCssStandardDeviation() {
        // CSS 30px blur: sigma 15; Android: 0.57735 r + 0.5 = 15.
        Assert.assertEquals(15f, 0.57735f * blurMaskRadius(30f) + 0.5f, 1e-3f)
        Assert.assertEquals(0.5f, blurMaskRadius(0f), 0f)
    }
}
