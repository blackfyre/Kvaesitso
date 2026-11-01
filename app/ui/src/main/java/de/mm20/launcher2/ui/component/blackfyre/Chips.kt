package de.mm20.launcher2.ui.component.blackfyre

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.mm20.launcher2.ui.theme.blackfyre.LocalBlackfyreDepth
import androidx.compose.material3.AssistChip as M3AssistChip
import androidx.compose.material3.FilterChip as M3FilterChip
import androidx.compose.material3.InputChip as M3InputChip

/*
 * Fork (blackfyre-components): Material 3 chips as small Blackfyre keys while the Blackfyre colour
 * theme is active, Material 3 otherwise. Signatures match the material3 1.5.0-beta02 overloads they
 * replace. A selected chip is a key held down.
 */

private val ChipCorner = 8.dp // bf-radius-md
private val ChipMinHeight = 32.dp
private val ChipIconSize = 18.dp

/** The design system's `micro` style: chips, tile labels and badges (10 px, 0.12 em, uppercase). */
@Composable
fun blackfyreMicro(): TextStyle = MaterialTheme.typography.labelSmall.merge(MicroMetrics)

internal val MicroMetrics = TextStyle(fontSize = 10.sp, letterSpacing = 0.12.em, lineHeight = 1.3.em)

@Composable
private fun ChipKey(
    action: KeyAction,
    label: @Composable () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    palette: KeyPalette,
    held: Boolean,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    horizontalArrangement: Arrangement.Horizontal,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource?,
) = Key(
    action = action,
    modifier = modifier,
    enabled = enabled,
    palette = palette,
    held = held,
    cornerRadius = ChipCorner,
    minWidth = 0.dp,
    minHeight = ChipMinHeight,
    contentPadding = contentPadding,
    textStyle = blackfyreMicro(),
    interactionSource = interactionSource,
    horizontalArrangement = horizontalArrangement,
) {
    if (leadingIcon != null) ChipIcon(leadingIcon)
    label()
    if (trailingIcon != null) ChipIcon(trailingIcon)
}

@Composable
private fun ChipIcon(icon: @Composable () -> Unit) {
    Box(Modifier.size(ChipIconSize), contentAlignment = Alignment.Center) { icon() }
}

/** The palette for a selectable chip: Blackfyre's unless the call site passed its own colours. */
@Composable
private fun selectablePalette(
    selected: Boolean,
    colors: SelectableChipColors,
    defaults: SelectableChipColors,
    dark: Boolean,
): KeyPalette = when {
    colors != defaults && selected -> KeyPalettes.custom(colors.selectedContainerColor, colors.selectedLabelColor)
    colors != defaults -> KeyPalettes.custom(colors.containerColor, colors.labelColor)
    selected -> KeyPalettes.pressed(dark)
    else -> KeyPalettes.secondary(dark)
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(enabled, selected),
    horizontalArrangement: Arrangement.Horizontal = FilterChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = FilterChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3FilterChip(
            selected, onClick, label, modifier, enabled, leadingIcon, trailingIcon, shape, colors,
            elevation, border, horizontalArrangement, contentPadding, interactionSource,
        )
        return
    }
    ChipKey(
        action = KeyAction.Toggle(selected, { onClick() }),
        label = label,
        modifier = modifier,
        enabled = enabled,
        palette = selectablePalette(selected, colors, FilterChipDefaults.filterChipColors(), depth.dark),
        held = selected,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}

@Composable
fun AssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    elevation: ChipElevation? = AssistChipDefaults.assistChipElevation(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled),
    horizontalArrangement: Arrangement.Horizontal = AssistChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = AssistChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3AssistChip(
            onClick, label, modifier, enabled, leadingIcon, trailingIcon, shape, colors, elevation,
            border, horizontalArrangement, contentPadding, interactionSource,
        )
        return
    }
    val palette = if (colors == AssistChipDefaults.assistChipColors()) KeyPalettes.secondary(depth.dark)
    else KeyPalettes.custom(colors.containerColor, colors.labelColor)
    ChipKey(
        action = KeyAction.Click(onClick),
        label = label,
        modifier = modifier,
        enabled = enabled,
        palette = palette,
        held = false,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}

@Composable
fun InputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    avatar: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = InputChipDefaults.shape,
    colors: SelectableChipColors = InputChipDefaults.inputChipColors(),
    elevation: SelectableChipElevation? = InputChipDefaults.inputChipElevation(),
    border: BorderStroke? = InputChipDefaults.inputChipBorder(enabled, selected),
    horizontalArrangement: Arrangement.Horizontal = InputChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues =
        InputChipDefaults.contentPadding(avatar != null, leadingIcon != null, trailingIcon != null),
    interactionSource: MutableInteractionSource? = null,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        M3InputChip(
            selected, onClick, label, modifier, enabled, leadingIcon, avatar, trailingIcon, shape,
            colors, elevation, border, horizontalArrangement, contentPadding, interactionSource,
        )
        return
    }
    ChipKey(
        action = KeyAction.Toggle(selected, { onClick() }),
        label = label,
        modifier = modifier,
        enabled = enabled,
        palette = selectablePalette(selected, colors, InputChipDefaults.inputChipColors(), depth.dark),
        held = selected,
        // Material draws the avatar in the leading slot when there is one.
        leadingIcon = avatar ?: leadingIcon,
        trailingIcon = trailingIcon,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}
