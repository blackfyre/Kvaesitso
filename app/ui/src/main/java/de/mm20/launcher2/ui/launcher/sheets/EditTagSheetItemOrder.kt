package de.mm20.launcher2.ui.launcher.sheets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
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
import de.mm20.launcher2.ui.component.dragndrop.LazyDragAndDropListState
import de.mm20.launcher2.ui.component.dragndrop.rememberLazyDragAndDropListState
import de.mm20.launcher2.ui.ktx.toPixels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Fork (pinned-tag-folders): the tag's items in stored order, reordered by long-press and drag, or
 * by dragging an item's handle straight away. The first item is what a tap on the tag's folder
 * launches.
 */
@Composable
fun TaggedItemsOrder(viewModel: EditTagSheetVM) {
    val iconSize = 32.dp.toPixels().toInt()
    FirstItemIconSwitch(viewModel, iconSize)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    lateinit var state: LazyDragAndDropListState
    state = rememberLazyDragAndDropListState(
        // The list moves an item only after it hovers 300 ms over a target; a quick drag and
        // release would snap back, so the item is also dropped where it is released.
        onDragEnd = { dragged -> dropAtRelease(state, dragged.key, viewModel::moveTaggedItem) },
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
                            modifier = Modifier.dragHandle(state, item.key, scope, haptic),
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

/**
 * Starts dragging the item [key] as soon as its handle is dragged, without the list's long-press.
 * Until it takes the gesture it consumes nothing, so a long-press drag that started first, on the
 * handle or elsewhere, keeps running.
 */
private fun Modifier.dragHandle(
    state: LazyDragAndDropListState,
    key: Any,
    scope: CoroutineScope,
    haptic: HapticFeedback,
) = pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var owned = false
        var moved = Offset.Zero
        while (true) {
            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
            if (change == null || !change.pressed) {
                if (owned) scope.launch { if (change == null) state.cancelDrag() else state.endDrag() }
                break
            }
            if (owned) {
                val delta = change.positionChange()
                change.consume()
                scope.launch { state.drag(Offset(0f, delta.y)) }
                continue
            }
            // A long-press drag, or another gesture, has taken over.
            if (state.draggedItem != null || change.isConsumed) break
            moved += change.positionChange()
            if (moved.getDistance() > viewConfiguration.touchSlop) {
                val layout = state.listState.layoutInfo
                val info = layout.visibleItemsInfo.find { it.key == key } ?: break
                owned = state.startDrag(Offset(0f, (info.offset + layout.beforeContentPadding).toFloat() + 1f))
                if (!owned) break
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                change.consume()
            }
        }
    }
}

/** Moves the dragged item [key] to the position under its centre, if that is another item's. */
private fun dropAtRelease(state: LazyDragAndDropListState, key: Any, move: (from: Int, to: Int) -> Unit) {
    val top = state.draggedItemAbsolutePosition?.y ?: return
    val items = state.listState.layoutInfo.visibleItemsInfo
    val dragged = items.find { it.key == key } ?: return
    val to = dropTargetIndex(top + dragged.size / 2f, items.map { Triple(it.index, it.offset, it.size) })
    if (to != null && to != dragged.index) move(dragged.index, to)
}

/** The index of the slot (index, offset, size) that contains [center], or null. */
internal fun dropTargetIndex(center: Float, slots: List<Triple<Int, Int, Int>>): Int? =
    slots.firstOrNull { (_, offset, size) -> center >= offset && center < offset + size }?.first
