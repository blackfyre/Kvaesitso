package de.mm20.launcher2.ui.component.blackfyre

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.mm20.launcher2.ui.theme.blackfyre.BlackfyrePanelShape
import de.mm20.launcher2.ui.theme.blackfyre.LocalBlackfyreDepth
import de.mm20.launcher2.ui.theme.blackfyre.blackfyreDialogShadow
import androidx.compose.material3.AlertDialog as M3AlertDialog

/**
 * Fork (blackfyre-components): Material 3's alert dialog, as a Blackfyre panel while the Blackfyre
 * colour theme is active: the panel radius (22 dp), `bf-surface` without a tonal tint, and
 * `bf-shadow-dialog`. The signature matches material3 1.5.0-beta02's.
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) {
    if (LocalBlackfyreDepth.current == null) {
        M3AlertDialog(
            onDismissRequest, confirmButton, modifier, dismissButton, icon, title, text, shape,
            containerColor, iconContentColor, titleContentColor, textContentColor, tonalElevation,
            properties,
        )
        return
    }
    M3AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier.blackfyreDialogShadow(BlackfyrePanelShape),
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = BlackfyrePanelShape,
        containerColor = if (containerColor == AlertDialogDefaults.containerColor) MaterialTheme.colorScheme.surface else containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = 0.dp,
        properties = properties,
    )
}
