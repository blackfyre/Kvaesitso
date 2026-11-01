package de.mm20.launcher2.ui.component.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.theme.blackfyre.LocalBlackfyreDepth
import de.mm20.launcher2.ui.theme.blackfyre.blackfyreKicker
import de.mm20.launcher2.ui.theme.blackfyre.blackfyrePanel
import de.mm20.launcher2.ui.theme.blackfyre.blackfyreShape


@Composable
fun PreferenceCategory(
    title: String? = null,
    iconPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (title != null) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = title,
                    // Fork (blackfyre-components): a mono-caps kicker in muted ink under Blackfyre.
                    style = blackfyreKicker(MaterialTheme.typography.titleSmall),
                    color = if (LocalBlackfyreDepth.current != null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.secondary
                )
            }
        }
        Column(
            // Fork (blackfyre-components): a raised panel under Blackfyre.
            modifier = Modifier
                .blackfyrePanel(blackfyreShape(MaterialTheme.shapes.medium))
                .clip(blackfyreShape(MaterialTheme.shapes.medium)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}