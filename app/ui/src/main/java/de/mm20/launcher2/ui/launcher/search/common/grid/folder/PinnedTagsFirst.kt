package de.mm20.launcher2.ui.launcher.search.common.grid.folder

import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.searchable.PinnedLevel
import de.mm20.launcher2.services.favorites.FavoritesService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Fork (folder-dock-and-first-item-icon): the pinned tags, in the Favourites editor's Tags order,
 * followed by [others] (called with the capacity left), at most [capacity] items in total.
 * Only pinned tags are included; [others] must exclude tags itself.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun FavoritesService.pinnedTagsFirst(
    capacity: Int,
    others: (limit: Int) -> Flow<List<SavableSearchable>>,
): Flow<List<SavableSearchable>> {
    if (capacity <= 0) return flowOf(emptyList())
    return getFavorites(
        includeTypes = listOf("tag"),
        minPinnedLevel = PinnedLevel.AutomaticallySorted,
        limit = capacity,
    ).flatMapLatest { tags ->
        val left = capacity - tags.size
        if (left <= 0) flowOf(tags.take(capacity))
        else others(left).map { tags + it.take(left) }
    }
}
