package de.mm20.launcher2.ui.launcher.widgets.clock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import de.mm20.launcher2.ui.locals.LocalDarkTheme

/**
 * Fork (blackfyre-theme): upstream's watch faces draw light content over a dark wallpaper in
 * `onPrimaryContainer` when the launcher is dark, assuming Material's tonal scheme, where that
 * role is a light tint. In a scheme whose `onPrimaryContainer` is the dark ink on a lighter accent
 * fill (Blackfyre: `bf-on-accent` #1b1b19 on `bf-accent`), the clock would vanish into the
 * wallpaper, so the watch faces get `primary` in that role instead (Blackfyre: `bf-accent-on-dark`).
 */
@Composable
fun ClockFaceColors(content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val onPrimaryContainer = clockFaceOnPrimaryContainer(
        dark = LocalDarkTheme.current,
        primary = scheme.primary,
        primaryContainer = scheme.primaryContainer,
        onPrimaryContainer = scheme.onPrimaryContainer,
    )
    if (onPrimaryContainer == scheme.onPrimaryContainer) {
        content()
    } else {
        MaterialTheme(colorScheme = scheme.copy(onPrimaryContainer = onPrimaryContainer), content = content)
    }
}

/**
 * The colour watch faces should use for `onPrimaryContainer`: [primary] when the launcher is dark
 * and [onPrimaryContainer] is darker than [primaryContainer], otherwise [onPrimaryContainer].
 */
internal fun clockFaceOnPrimaryContainer(
    dark: Boolean,
    primary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
): Color {
    if (!dark) return onPrimaryContainer
    return if (onPrimaryContainer.luminance() < primaryContainer.luminance()) primary else onPrimaryContainer
}
