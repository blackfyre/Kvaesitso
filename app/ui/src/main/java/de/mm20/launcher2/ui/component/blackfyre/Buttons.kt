package de.mm20.launcher2.ui.component.blackfyre

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.theme.blackfyre.LocalBlackfyreDepth
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.FilledIconButton as M3FilledIconButton
import androidx.compose.material3.FilledTonalButton as M3FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton as M3FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton as M3FloatingActionButton
import androidx.compose.material3.OutlinedButton as M3OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton as M3SmallFloatingActionButton

/*
 * Fork (blackfyre-components): Material 3 buttons as Blackfyre keycaps while the Blackfyre colour
 * theme is active, and Material 3 otherwise. Each function has the signature of the Material 3
 * overload it replaces (material3 1.5.0-beta02), so call sites switch by import alone
 * (`mise run controls`). Under Blackfyre the key defines shape, elevation and border; colours a
 * call site passes, other than the defaults, are kept.
 */

private val KeyCorner = 10.dp
private val KeyMinHeight = 44.dp
private val KeyMinWidth = 64.dp
private val IconKeySize = 44.dp

/** The key palette for [colors]: [default] unless the call site passed its own colours. */
@Composable
private fun buttonPalette(colors: ButtonColors, defaults: ButtonColors, default: KeyPalette) =
    if (colors == defaults) default else KeyPalettes.custom(colors.containerColor, colors.contentColor)

@Composable
private fun iconButtonPalette(colors: IconButtonColors, defaults: IconButtonColors, default: KeyPalette) =
    if (colors == defaults) default else KeyPalettes.custom(colors.containerColor, colors.contentColor)

@Composable
private fun ButtonKey(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    palette: KeyPalette,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource?,
    content: @Composable RowScope.() -> Unit,
) = Key(
    action = KeyAction.Click(onClick),
    modifier = modifier,
    enabled = enabled,
    palette = palette,
    held = false,
    cornerRadius = KeyCorner,
    minWidth = KeyMinWidth,
    minHeight = KeyMinHeight,
    contentPadding = contentPadding,
    textStyle = MaterialTheme.typography.labelLarge,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (LocalBlackfyreDepth.current == null) {
        M3Button(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)
        return
    }
    ButtonKey(
        onClick, modifier, enabled,
        buttonPalette(colors, ButtonDefaults.buttonColors(), KeyPalettes.Primary),
        contentPadding, interactionSource, content,
    )
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.filledTonalShape,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3FilledTonalButton(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)
        return
    }
    ButtonKey(
        onClick, modifier, enabled,
        buttonPalette(colors, ButtonDefaults.filledTonalButtonColors(), KeyPalettes.secondary(depth.dark)),
        contentPadding, interactionSource, content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3FilledTonalButton(onClick, shapes, modifier, enabled, colors, elevation, border, contentPadding, interactionSource, content)
        return
    }
    ButtonKey(
        onClick, modifier, enabled,
        buttonPalette(colors, ButtonDefaults.filledTonalButtonColors(), KeyPalettes.secondary(depth.dark)),
        contentPadding, interactionSource, content,
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3OutlinedButton(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)
        return
    }
    val defaults = ButtonDefaults.outlinedButtonColors()
    // Outlined buttons have a transparent container; a call site's own colours are honoured on
    // the light key by their content colour only, unless they set a container.
    val palette = when {
        colors == defaults -> KeyPalettes.secondary(depth.dark)
        colors.containerColor == Color.Transparent ->
            KeyPalettes.secondary(depth.dark).copy(content = colors.contentColor)
        else -> KeyPalettes.custom(colors.containerColor, colors.contentColor)
    }
    ButtonKey(onClick, modifier, enabled, palette, contentPadding, interactionSource, content)
}

@Composable
private fun IconKey(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    palette: KeyPalette,
    interactionSource: MutableInteractionSource?,
    content: @Composable () -> Unit,
) = Key(
    action = KeyAction.Click(onClick),
    modifier = modifier,
    enabled = enabled,
    palette = palette,
    held = false,
    cornerRadius = KeyCorner,
    minWidth = IconKeySize,
    minHeight = IconKeySize,
    contentPadding = PaddingValues(0.dp),
    textStyle = MaterialTheme.typography.labelLarge,
    interactionSource = interactionSource,
) { content() }

@Composable
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    if (LocalBlackfyreDepth.current == null) {
        M3FilledIconButton(onClick, modifier, enabled, shape, colors, interactionSource, content)
        return
    }
    IconKey(
        onClick, modifier, enabled,
        iconButtonPalette(colors, IconButtonDefaults.filledIconButtonColors(), KeyPalettes.Primary),
        interactionSource, content,
    )
}

@Composable
fun FilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3FilledTonalIconButton(onClick, modifier, enabled, shape, colors, interactionSource, content)
        return
    }
    IconKey(
        onClick, modifier, enabled,
        iconButtonPalette(colors, IconButtonDefaults.filledTonalIconButtonColors(), KeyPalettes.secondary(depth.dark)),
        interactionSource, content,
    )
}

/** The palette for a floating action button: the orange key unless the call site set a colour. */
@Composable
private fun fabPalette(containerColor: Color, contentColor: Color) =
    if (containerColor == FloatingActionButtonDefaults.containerColor) KeyPalettes.Primary
    else KeyPalettes.custom(containerColor, contentColor)

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    if (LocalBlackfyreDepth.current == null) {
        M3FloatingActionButton(onClick, modifier, shape, containerColor, contentColor, elevation, interactionSource, content)
        return
    }
    Key(
        action = KeyAction.Click(onClick),
        modifier = modifier,
        enabled = true,
        palette = fabPalette(containerColor, contentColor),
        held = false,
        cornerRadius = KeyCorner,
        minWidth = 56.dp,
        minHeight = 56.dp,
        contentPadding = PaddingValues(0.dp),
        textStyle = MaterialTheme.typography.labelLarge,
        interactionSource = interactionSource,
    ) { content() }
}

@Composable
fun SmallFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.smallShape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    if (LocalBlackfyreDepth.current == null) {
        M3SmallFloatingActionButton(onClick, modifier, shape, containerColor, contentColor, elevation, interactionSource, content)
        return
    }
    Key(
        action = KeyAction.Click(onClick),
        modifier = modifier,
        enabled = true,
        palette = fabPalette(containerColor, contentColor),
        held = false,
        cornerRadius = KeyCorner,
        minWidth = IconKeySize,
        minHeight = IconKeySize,
        contentPadding = PaddingValues(0.dp),
        textStyle = MaterialTheme.typography.labelLarge,
        interactionSource = interactionSource,
    ) { content() }
}
