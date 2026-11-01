package de.mm20.launcher2.ui.launcher.sheets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.mm20.launcher2.data.customattrs.FirstItemIcon
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.ShapedLauncherIcon
import de.mm20.launcher2.ui.component.dragndrop.DraggableItem
import de.mm20.launcher2.ui.component.dragndrop.LazyDragAndDropColumn
import de.mm20.launcher2.ui.component.dragndrop.rememberLazyDragAndDropListState
import de.mm20.launcher2.ui.ktx.toPixels
import kotlinx.coroutines.flow.flowOf

/**
 * Fork (pinned-tag-folders): the tag's items in stored order, reordered by long-press and drag.
 * The first item is what a tap on the tag's folder launches.
 */
@Composable
fun TaggedItemsOrder(viewModel: EditTagSheetVM) {
    val iconSize = 32.dp.toPixels().toInt()
    FirstItemIconSwitch(viewModel, iconSize)
    val state = rememberLazyDragAndDropListState(
        onItemMove = { from, to -> viewModel.moveTaggedItem(from.index, to.index) },
    )
    LazyDragAndDropColumn(
        state = state,
        // Bounded so the list can sit inside the page's vertical scroll.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 9999.dp),
        bidirectionalDrag = false,
        userScrollEnabled = false,
    ) {
        items(viewModel.taggedItems, key = { it.key }) { item ->
            DraggableItem(state = state, key = item.key) { dragged ->
                val elevation by animateDpAsState(if (dragged) 4.dp else 0.dp)
                val icon by remember(item.key) { viewModel.getIcon(item, iconSize) }.collectAsState(null)
                Surface(
                    shadowElevation = elevation,
                    tonalElevation = elevation,
                    modifier = Modifier.zIndex(if (dragged) 1f else 0f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShapedLauncherIcon(size = 32.dp, icon = { icon })
                        Text(
                            text = item.labelOverride ?: item.label,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            painterResource(R.drawable.drag_indicator_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fork (folder-dock-and-first-item-icon): use the first item's icon as the folder icon. Stored as
 * the tag's custom icon, so picking an icon or emoji turns it off.
 */
@Composable
private fun FirstItemIconSwitch(viewModel: EditTagSheetVM, iconSize: Int) {
    val customIcon by viewModel.tagCustomIcon.collectAsState()
    val first = viewModel.taggedItems.firstOrNull()
    val icon by remember(first?.key) {
        first?.let { viewModel.getIcon(it, iconSize) } ?: flowOf(null)
    }.collectAsState(null)
    val checked = customIcon is FirstItemIcon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = { viewModel.selectIcon(if (it) FirstItemIcon else null) },
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShapedLauncherIcon(size = 32.dp, icon = { icon })
        Text(
            text = stringResource(R.string.tag_folder_first_item_icon),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}
