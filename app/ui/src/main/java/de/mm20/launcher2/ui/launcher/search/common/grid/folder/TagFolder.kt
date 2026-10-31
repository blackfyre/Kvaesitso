package de.mm20.launcher2.ui.launcher.search.common.grid.folder

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.badges.Badge
import de.mm20.launcher2.icons.LauncherIcon
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.search.Tag
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.ShapedLauncherIcon
import de.mm20.launcher2.ui.ktx.toPixels
import de.mm20.launcher2.ui.launcher.search.common.grid.SearchResultGrid
import de.mm20.launcher2.ui.launcher.search.listItemViewModel
import de.mm20.launcher2.ui.launcher.sheets.LocalBottomSheetManager
import de.mm20.launcher2.ui.component.LocalIconShape

// Fork (pinned-tag-folders): folder UI for tags in the item grid. GridItem hooks in through
// FolderAwareLauncherIcon, rememberTagFolderLaunch and TagItemGridPopup.

@Composable
private fun tagFolderViewModel(tag: Tag, size: Dp): TagFolderVM {
    val viewModel: TagFolderVM = listItemViewModel(key = "folder-${tag.key}")
    val sizePx = size.toPixels().toInt()
    LaunchedEffect(tag, sizePx) { viewModel.init(tag, sizePx) }
    return viewModel
}

/**
 * Draws [item]'s icon like [ShapedLauncherIcon], except that a [Tag] is drawn as a folder: its
 * custom icon if it has one, otherwise a mini-grid of its first items.
 */
@Composable
fun FolderAwareLauncherIcon(
    item: SavableSearchable,
    modifier: Modifier = Modifier,
    size: Dp,
    icon: () -> LauncherIcon? = { null },
    badge: () -> Badge? = { null },
) {
    if (item !is Tag) {
        ShapedLauncherIcon(modifier = modifier, size = size, icon = icon, badge = badge)
        return
    }
    val viewModel = tagFolderViewModel(item, size)
    // Unknown until loaded: render nothing rather than flash the preview before a custom icon.
    val customIcon by produceState<Result<LauncherIcon?>?>(null, viewModel) {
        viewModel.customIcon.collect { value = Result.success(it) }
    }
    val custom = customIcon ?: run {
        Box(modifier.size(size))
        return
    }
    if (custom.getOrNull() != null) {
        ShapedLauncherIcon(modifier = modifier, size = size, icon = { custom.getOrNull() }, badge = badge)
    } else {
        FolderPreviewIcon(modifier = modifier, size = size, viewModel = viewModel)
    }
}

@Composable
private fun FolderPreviewIcon(modifier: Modifier, size: Dp, viewModel: TagFolderVM) {
    val icons by viewModel.previewIcons.collectAsState(emptyList())
    val shape = LocalIconShape.current
    val gap = size * 0.08f
    val cell = (size - gap * 3) / 2
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(gap),
    ) {
        Column {
            for (row in 0 until 2) {
                if (row > 0) Spacer(Modifier.size(gap))
                Row {
                    for (col in 0 until 2) {
                        if (col > 0) Spacer(Modifier.width(gap))
                        val i = row * 2 + col
                        if (i < icons.size) {
                            ShapedLauncherIcon(size = cell, icon = { icons[i] })
                        } else {
                            Spacer(Modifier.size(cell))
                        }
                    }
                }
            }
        }
    }
}

/**
 * For a [Tag], returns the tap action of its folder: launch the first item, or call the fallback
 * (open the folder) if nothing launches. Returns null for any other item.
 */
@Composable
fun rememberTagFolderLaunch(
    item: SavableSearchable,
    size: Dp,
): ((context: Context, bounds: IntRect?, onNothingLaunched: () -> Unit) -> Unit)? {
    if (item !is Tag) return null
    val viewModel = tagFolderViewModel(item, size)
    return viewModel::launchFirst
}

/** The opened folder: the tag's name, an edit action and its items in stored order. */
@Composable
fun TagItemGridPopup(
    tag: Tag,
    show: MutableTransitionState<Boolean>,
    animationProgress: Float,
    origin: IntRect,
    onDismiss: () -> Unit,
) {
    // Own key: the grid icon's view model carries the icon size, which this one must not change.
    val viewModel: TagFolderVM = listItemViewModel(key = "folder-popup-${tag.key}")
    LaunchedEffect(tag) { viewModel.init(tag, 0) }
    val items by viewModel.items.collectAsState()
    val sheetManager = LocalBottomSheetManager.current

    AnimatedVisibility(
        show,
        enter = expandIn(animationSpec = tween(300), expandFrom = Alignment.Center) { origin.size },
        exit = shrinkOut(animationSpec = tween(300), shrinkTowards = Alignment.Center) { origin.size },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tag.labelOverride ?: tag.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = {
                    sheetManager.showEditTagSheet(tag.tag)
                    onDismiss()
                }) {
                    Icon(painterResource(R.drawable.edit_24px), stringResource(R.string.edit))
                }
            }
            SearchResultGrid(items = items, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}
