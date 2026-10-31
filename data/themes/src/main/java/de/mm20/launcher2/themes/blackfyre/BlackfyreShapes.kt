package de.mm20.launcher2.themes.blackfyre

import de.mm20.launcher2.themes.BlackfyreThemeId
import de.mm20.launcher2.themes.shapes.CornerStyle
import de.mm20.launcher2.themes.shapes.Shape
import de.mm20.launcher2.themes.shapes.Shapes

// Blackfyre shapes (OpenSpec change blackfyre-theme, design decision 6): rounded corners
// from the radius tokens in tokens.json.

private fun radius(dp: Int) = Shape(radii = intArrayOf(dp, dp, dp, dp))

val BlackfyreShapes = Shapes(
    id = BlackfyreThemeId,
    builtIn = true,
    name = "Blackfyre",
    baseShape = Shape(
        corners = CornerStyle.Rounded,
        radii = intArrayOf(12, 12, 12, 12), // bf-radius-xl
    ),
    extraSmall = radius(4), // bf-radius-xs
    small = radius(8), // bf-radius-md
    medium = radius(12), // bf-radius-xl
    large = radius(14), // bf-radius-2xl
    largeIncreased = radius(22), // bf-radius-panel
    extraLarge = radius(22), // bf-radius-panel
    extraLargeIncreased = radius(22), // bf-radius-panel
    extraExtraLarge = radius(22), // bf-radius-panel
)
