package de.mm20.launcher2.ui.launcher.search.common.grid.folder

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.unit.IntRect
import androidx.core.app.ActivityOptionsCompat
import de.mm20.launcher2.data.customattrs.CustomAttributesRepository
import de.mm20.launcher2.data.customattrs.FirstItemIcon
import de.mm20.launcher2.data.customattrs.utils.withCustomLabels
import de.mm20.launcher2.icons.IconService
import de.mm20.launcher2.icons.LauncherIcon
import de.mm20.launcher2.search.AppShortcut
import de.mm20.launcher2.search.Application
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.search.Tag
import de.mm20.launcher2.services.favorites.FavoritesService
import de.mm20.launcher2.services.tags.TagsService
import de.mm20.launcher2.ui.launcher.search.ListItemViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Number of items shown in a folder's preview icon. */
internal const val FolderPreviewSize = 4

/**
 * Fork (pinned-tag-folders): a pinned tag shown as a folder. Items are in the tag's stored order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagFolderVM : ListItemViewModel(), KoinComponent {
    private val tagsService: TagsService by inject()
    private val iconService: IconService by inject()
    private val favoritesService: FavoritesService by inject()
    private val customAttributesRepository: CustomAttributesRepository by inject()

    private val tag = MutableStateFlow<Tag?>(null)
    private val iconSize = MutableStateFlow(0)

    fun init(tag: Tag, iconSize: Int) {
        this.tag.value = tag
        this.iconSize.value = iconSize
    }

    val items: StateFlow<List<SavableSearchable>> = tag.flatMapLatest {
        if (it == null) emptyFlow()
        else tagsService.getTaggedItems(it.tag).withCustomLabels(customAttributesRepository)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /**
     * The tag's custom icon, or null if it has none. Stays unset (no emission) until known, so the
     * folder does not flash its preview before a custom icon loads.
     */
    val customIcon: Flow<LauncherIcon?> = combine(tag, iconSize) { t, s -> t to s }
        .flatMapLatest { (t, size) ->
            if (t == null || size == 0) emptyFlow()
            else iconService.getCustomIcon(t).flatMapLatest { custom ->
                when (custom) {
                    null -> flowOf(null)
                    // Fork (folder-dock-and-first-item-icon): the first item's icon, or the
                    // (empty) preview when the tag has no items.
                    is FirstItemIcon -> tagsService.getTaggedItems(t.tag).flatMapLatest { items ->
                        items.firstOrNull()?.let { iconService.getIcon(it, size) } ?: flowOf(null)
                    }
                    else -> iconService.resolveCustomIcon(t, size, custom)
                }
            }
        }

    /** Icons of the first [FolderPreviewSize] items; an entry is null while its icon loads. */
    val previewIcons: Flow<List<LauncherIcon?>> = combine(items, iconSize) { i, s -> i to s }
        .flatMapLatest { (items, size) ->
            val first = items.take(FolderPreviewSize)
            if (first.isEmpty() || size == 0) flowOf(emptyList())
            else combine(first.map { item ->
                iconService.getIcon(item, size / 2).onStart { emit(null) }
            }) { it.toList() }
        }

    /**
     * Launches the first item, in stored order, that launches successfully. Calls [onNothingLaunched]
     * if none does, so the caller can open the folder instead.
     */
    fun launchFirst(context: Context, bounds: IntRect?, onNothingLaunched: () -> Unit) {
        val tag = tag.value ?: return onNothingLaunched()
        viewModelScope.launch {
            val items = tagsService.getTaggedItems(tag.tag).first()
            val bundle = launchOptions(context, bounds)
            for (item in items) {
                if (item.launch(context, bundle)) {
                    favoritesService.reportLaunch(item)
                    return@launch
                } else if (item is Application || item is AppShortcut) {
                    favoritesService.reset(item)
                }
            }
            onNothingLaunched()
        }
    }

    private fun launchOptions(context: Context, bounds: IntRect?) =
        (context as? AppCompatActivity)?.window?.decorView?.takeIf { bounds != null }?.let { view ->
            ActivityOptionsCompat.makeScaleUpAnimation(
                view, bounds!!.left, bounds.top, bounds.width, bounds.height,
            )
        }.let { it ?: ActivityOptionsCompat.makeBasic() }.toBundle()
}
