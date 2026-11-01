package de.mm20.launcher2.ui.component.blackfyre

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonElevation
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.ToggleButtonSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.theme.blackfyre.LocalBlackfyreDepth
import androidx.compose.material3.ToggleButton as M3ToggleButton

/*
 * Fork (blackfyre-components): the Material 3 expressive toggle button as a small Blackfyre key
 * while the Blackfyre colour theme is active, Material 3 otherwise. Checked is a key held down.
 * Under Blackfyre, [shapes] (Material's shape morphing and connected-group corners) is ignored:
 * grouped toggles become separate keys.
 */

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: ToggleButtonSize = ToggleButtonDefaults.size,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    shapes: ToggleButtonShapes = ToggleButtonDefaults.shapesFor(buttonSize),
    colors: ToggleButtonColors = ToggleButtonDefaults.colors(),
    elevation: ToggleButtonElevation? = ToggleButtonDefaults.elevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues =
        ToggleButtonDefaults.contentPaddingFor(buttonSize, hasStartIcon = icon != null),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3ToggleButton(
            checked, onCheckedChange, modifier, buttonSize, enabled, icon, shapes, colors, elevation,
            border, contentPadding, interactionSource, content,
        )
        return
    }
    val palette = when {
        colors != ToggleButtonDefaults.colors() && checked ->
            KeyPalettes.custom(colors.checkedContainerColor, colors.checkedContentColor)
        colors != ToggleButtonDefaults.colors() -> KeyPalettes.custom(colors.containerColor, colors.contentColor)
        checked -> KeyPalettes.pressed(depth.dark)
        else -> KeyPalettes.secondary(depth.dark)
    }
    Key(
        action = KeyAction.Toggle(checked, onCheckedChange, Role.Checkbox),
        modifier = modifier,
        enabled = enabled,
        palette = palette,
        held = checked,
        cornerRadius = 8.dp,
        minWidth = 0.dp,
        minHeight = 32.dp,
        contentPadding = contentPadding,
        textStyle = blackfyreMicro(),
        interactionSource = interactionSource,
    ) {
        icon?.invoke()
        content()
    }
}
