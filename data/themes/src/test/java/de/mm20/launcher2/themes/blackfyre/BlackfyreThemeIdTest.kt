package de.mm20.launcher2.themes.blackfyre

import de.mm20.launcher2.preferences.LauncherSettingsData
import de.mm20.launcher2.themes.BlackfyreThemeId
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import java.util.UUID

/**
 * core:preferences cannot depend on data:themes, so LauncherSettingsData repeats
 * BlackfyreThemeId as its default theme ID. This keeps the two equal.
 */
class BlackfyreThemeIdTest {

    @Test
    fun settingsDefaultToBlackfyre() {
        val defaults = Json { ignoreUnknownKeys = true }.decodeFromString<LauncherSettingsData>("{}")
        Assert.assertEquals(BlackfyreThemeId, defaults.uiColorsId)
        Assert.assertEquals(BlackfyreThemeId, defaults.uiShapesId)
        Assert.assertEquals(BlackfyreThemeId, defaults.uiTypographyId)
        Assert.assertEquals(UUID(0L, 0L), defaults.uiTransparenciesId)
    }
}
