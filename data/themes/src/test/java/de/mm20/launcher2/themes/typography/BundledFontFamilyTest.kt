package de.mm20.launcher2.themes.typography

import de.mm20.launcher2.themes.ThemeJson
import org.junit.Assert
import org.junit.Test

class BundledFontFamilyTest {

    @Test
    fun bundledFamilySurvivesJsonRoundTrip() {
        val fonts: Map<String, FontFamily?> = mapOf(
            "plain" to FontFamily.Bundled("archivo"),
            "caps" to FontFamily.Bundled("bf_mono_caps", mapOf("wght" to 500f)),
            "brand" to null,
        )
        val json = ThemeJson.encodeToString(fonts)
        Assert.assertTrue(json, json.contains("\"bundled\""))
        Assert.assertEquals(fonts, ThemeJson.decodeFromString<Map<String, FontFamily?>>(json))
    }
}
