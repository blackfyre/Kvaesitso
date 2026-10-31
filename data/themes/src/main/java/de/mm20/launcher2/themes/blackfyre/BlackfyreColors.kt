package de.mm20.launcher2.themes.blackfyre

import de.mm20.launcher2.themes.BlackfyreThemeId
import de.mm20.launcher2.themes.colors.ColorScheme
import de.mm20.launcher2.themes.colors.Colors
import de.mm20.launcher2.themes.colors.EmptyCorePalette
import de.mm20.launcher2.themes.colors.StaticColor

// Blackfyre colour schemes (OpenSpec change blackfyre-theme, design decision 2).
// Values are the Blackfyre Design System's tokens.json (version 1), mapped by token name:
// Paper is the light scheme, Dark the dark scheme. A token without a Dark value keeps its
// Paper value. Every role is a static colour; the core palette stays empty.

val BlackfyreLightColorScheme = ColorScheme(
    primary = StaticColor(0xFFA3390C.toInt()), // bf-accent-text #a3390c
    onPrimary = StaticColor(0xFFFFFFFF.toInt()), // bf-white #ffffff
    primaryContainer = StaticColor(0xFFE2551D.toInt()), // bf-accent #e2551d
    onPrimaryContainer = StaticColor(0xFF1B1B19.toInt()), // bf-on-accent #1b1b19
    inversePrimary = StaticColor(0xFFFF8A4C.toInt()), // bf-accent-on-dark #ff8a4c
    secondary = StaticColor(0xFF1B1B19.toInt()), // bf-ink #1b1b19
    onSecondary = StaticColor(0xFFF3F2EE.toInt()), // bf-on-panel #f3f2ee
    secondaryContainer = StaticColor(0xFFDEDBD4.toInt()), // bf-surface-sunk #dedbd4
    onSecondaryContainer = StaticColor(0xFF1B1B19.toInt()), // bf-ink #1b1b19
    tertiary = StaticColor(0xFF4A4944.toInt()), // bf-ink-soft #4a4944
    onTertiary = StaticColor(0xFFF3F2EE.toInt()), // bf-surface #f3f2ee
    tertiaryContainer = StaticColor(0xFFD9D6CF.toInt()), // bf-paper-deep #d9d6cf
    onTertiaryContainer = StaticColor(0xFF1B1B19.toInt()), // bf-ink #1b1b19
    error = StaticColor(0xFFC2301F.toInt()), // bf-alert-text #c2301f
    onError = StaticColor(0xFFFFFFFF.toInt()), // bf-white #ffffff
    errorContainer = StaticColor(0xFFF8F7F4.toInt()), // bf-surface-hover #f8f7f4
    onErrorContainer = StaticColor(0xFFC2301F.toInt()), // bf-alert-text #c2301f
    background = StaticColor(0xFFE6E4DF.toInt()), // bf-paper #e6e4df
    onBackground = StaticColor(0xFF1B1B19.toInt()), // bf-ink #1b1b19
    surface = StaticColor(0xFFF3F2EE.toInt()), // bf-surface #f3f2ee
    onSurface = StaticColor(0xFF1B1B19.toInt()), // bf-ink #1b1b19
    surfaceVariant = StaticColor(0xFFDEDBD4.toInt()), // bf-surface-sunk #dedbd4
    onSurfaceVariant = StaticColor(0xFF4A4944.toInt()), // bf-ink-soft #4a4944
    surfaceContainerLowest = StaticColor(0xFFFFFFFF.toInt()), // bf-highlight #ffffff
    surfaceContainerLow = StaticColor(0xFFF8F7F4.toInt()), // bf-surface-hover #f8f7f4
    surfaceContainer = StaticColor(0xFFF3F2EE.toInt()), // bf-surface #f3f2ee
    surfaceContainerHigh = StaticColor(0xFFE6E4DF.toInt()), // bf-paper #e6e4df
    surfaceContainerHighest = StaticColor(0xFFDEDBD4.toInt()), // bf-surface-sunk #dedbd4
    surfaceDim = StaticColor(0xFFD9D6CF.toInt()), // bf-paper-deep #d9d6cf
    surfaceBright = StaticColor(0xFFF8F7F4.toInt()), // bf-surface-hover #f8f7f4
    surfaceTint = StaticColor(0xFFF3F2EE.toInt()), // bf-surface #f3f2ee
    outline = StaticColor(0xFFBFBBB3.toInt()), // bf-line-strong #bfbbb3
    outlineVariant = StaticColor(0xFFD3D0C9.toInt()), // bf-line #d3d0c9
    inverseSurface = StaticColor(0xFF1B1B19.toInt()), // bf-panel #1b1b19
    inverseOnSurface = StaticColor(0xFFF3F2EE.toInt()), // bf-on-panel #f3f2ee
    scrim = StaticColor(0xFF000000.toInt()), // bf-panel-edge #000000
)

val BlackfyreDarkColorScheme = ColorScheme(
    primary = StaticColor(0xFFFF8A4C.toInt()), // bf-accent-text #ff8a4c
    onPrimary = StaticColor(0xFF1B1B19.toInt()), // bf-on-accent #1b1b19
    primaryContainer = StaticColor(0xFFE2551D.toInt()), // bf-accent #e2551d
    onPrimaryContainer = StaticColor(0xFF1B1B19.toInt()), // bf-on-accent #1b1b19
    inversePrimary = StaticColor(0xFFA3390C.toInt()), // bf-accent-text (Paper) #a3390c
    secondary = StaticColor(0xFFF3F2EE.toInt()), // bf-ink #f3f2ee
    onSecondary = StaticColor(0xFF1B1B19.toInt()), // bf-on-accent #1b1b19
    secondaryContainer = StaticColor(0xFF2C2C29.toInt()), // bf-surface-sunk #2c2c29
    onSecondaryContainer = StaticColor(0xFFF3F2EE.toInt()), // bf-ink #f3f2ee
    tertiary = StaticColor(0xFFC9C6BF.toInt()), // bf-ink-soft #c9c6bf
    onTertiary = StaticColor(0xFF1B1B19.toInt()), // bf-on-accent #1b1b19
    tertiaryContainer = StaticColor(0xFF161615.toInt()), // bf-paper-deep #161615
    onTertiaryContainer = StaticColor(0xFFF3F2EE.toInt()), // bf-ink #f3f2ee
    error = StaticColor(0xFFFF6A4C.toInt()), // bf-alert-text #ff6a4c
    onError = StaticColor(0xFF1B1B19.toInt()), // bf-on-accent #1b1b19
    errorContainer = StaticColor(0xFF2C2C29.toInt()), // bf-surface-sunk #2c2c29
    onErrorContainer = StaticColor(0xFFFF6A4C.toInt()), // bf-alert-text #ff6a4c
    background = StaticColor(0xFF1B1B19.toInt()), // bf-paper #1b1b19
    onBackground = StaticColor(0xFFF3F2EE.toInt()), // bf-ink #f3f2ee
    surface = StaticColor(0xFF242421.toInt()), // bf-surface #242421
    onSurface = StaticColor(0xFFF3F2EE.toInt()), // bf-ink #f3f2ee
    surfaceVariant = StaticColor(0xFF2C2C29.toInt()), // bf-surface-sunk #2c2c29
    onSurfaceVariant = StaticColor(0xFFC9C6BF.toInt()), // bf-ink-soft #c9c6bf
    surfaceContainerLowest = StaticColor(0xFF161615.toInt()), // bf-paper-deep #161615
    surfaceContainerLow = StaticColor(0xFF1B1B19.toInt()), // bf-paper #1b1b19
    surfaceContainer = StaticColor(0xFF242421.toInt()), // bf-surface #242421
    surfaceContainerHigh = StaticColor(0xFF2C2C29.toInt()), // bf-surface-hover #2c2c29
    surfaceContainerHighest = StaticColor(0xFF3A3935.toInt()), // bf-card-edge #3a3935
    surfaceDim = StaticColor(0xFF161615.toInt()), // bf-paper-deep #161615
    surfaceBright = StaticColor(0xFF2C2C29.toInt()), // bf-surface-hover #2c2c29
    surfaceTint = StaticColor(0xFF242421.toInt()), // bf-surface #242421
    outline = StaticColor(0xFF3A3935.toInt()), // bf-card-edge #3a3935
    outlineVariant = StaticColor(0xFF33332F.toInt()), // bf-line #33332f
    inverseSurface = StaticColor(0xFFF3F2EE.toInt()), // bf-ink #f3f2ee
    inverseOnSurface = StaticColor(0xFF1B1B19.toInt()), // bf-paper #1b1b19
    scrim = StaticColor(0xFF000000.toInt()), // bf-panel-edge #000000
)

val BlackfyreColors = Colors(
    id = BlackfyreThemeId,
    builtIn = true,
    name = "Blackfyre",
    corePalette = EmptyCorePalette,
    lightColorScheme = BlackfyreLightColorScheme,
    darkColorScheme = BlackfyreDarkColorScheme,
)
