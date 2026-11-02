package de.mm20.launcher2.ui.launcher.search.common.grid.folder

import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.search.Tag
import de.mm20.launcher2.searchable.PinnedLevel
import de.mm20.launcher2.services.favorites.FavoritesService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Fork (folder-dock-and-first-item-icon, dock-folder-opt-in): the pinned tags whose "Show in dock"
 * option is on ([docked] returns those names among the ones it is given), in the Favourites
 * editor's Tags order, followed by [others] (called with the capacity left), at most [capacity]
 * items in total. [others] must exclude tags itself.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun FavoritesService.pinnedTagsFirst(
    capacity: Int,
    docked: (tags: List<String>) -> Flow<Set<String>>,
    others: (limit: Int) -> Flow<List<SavableSearchable>>,
): Flow<List<SavableSearchable>> {
    if (capacity <= 0) return flowOf(emptyList())
    return getFavorites(
        includeTypes = listOf("tag"),
        minPinnedLevel = PinnedLevel.AutomaticallySorted,
    ).flatMapLatest { pinned ->
        val names = pinned.mapNotNull { (it as? Tag)?.tag }
        docked(names).map { dockedFolders(pinned, it, capacity) }
    }.flatMapLatest { tags ->
        val left = capacity - tags.size
        if (left <= 0) flowOf(tags)
        else others(left).map { tags + it.take(left) }
    }
}

/** The [pinned] tags whose names are in [docked], in order, at most [capacity] of them. */
internal fun dockedFolders(
    pinned: List<SavableSearchable>,
    docked: Set<String>,
    capacity: Int,
): List<SavableSearchable> =
    pinned.filter { (it as? Tag)?.tag in docked }.take(capacity.coerceAtLeast(0))
