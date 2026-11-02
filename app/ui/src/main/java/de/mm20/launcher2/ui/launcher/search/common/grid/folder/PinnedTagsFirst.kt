package de.mm20.launcher2.ui.launcher.search.common.grid.folder

import de.mm20.launcher2.data.customattrs.FolderDockRepository
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
 * option is on, in the Favourites editor's Tags order, followed by [others] without the items of
 * those folders, at most [capacity] items in total. [others] is called with the number of items it
 * should return and must exclude tags itself.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun FavoritesService.pinnedTagsFirst(
    capacity: Int,
    folderDock: FolderDockRepository,
    others: (limit: Int) -> Flow<List<SavableSearchable>>,
): Flow<List<SavableSearchable>> {
    if (capacity <= 0) return flowOf(emptyList())
    return getFavorites(
        includeTypes = listOf("tag"),
        minPinnedLevel = PinnedLevel.AutomaticallySorted,
    ).flatMapLatest { pinned ->
        folderDock.docked(pinned.tagNames()).map { dockedFolders(pinned, it, capacity) }
    }.flatMapLatest { folders ->
        val left = capacity - folders.size
        if (left <= 0) flowOf(folders)
        else folderDock.itemsIn(folders.tagNames()).flatMapLatest { inFolders ->
            // Ask for enough items that removing the folders' own items still fills the dock.
            others(left + inFolders.size).map { dockItems(folders, it, inFolders, capacity) }
        }
    }
}

private fun List<SavableSearchable>.tagNames() = mapNotNull { (it as? Tag)?.tag }

/** The [pinned] tags whose names are in [docked], in order, at most [capacity] of them. */
internal fun dockedFolders(
    pinned: List<SavableSearchable>,
    docked: Set<String>,
    capacity: Int,
): List<SavableSearchable> =
    pinned.filter { (it as? Tag)?.tag in docked }.take(capacity.coerceAtLeast(0))

/** [folders], then [others] without the items whose keys are in [inFolders], at most [capacity]. */
internal fun dockItems(
    folders: List<SavableSearchable>,
    others: List<SavableSearchable>,
    inFolders: Set<String>,
    capacity: Int,
): List<SavableSearchable> =
    (folders + others.filter { it.key !in inFolders }).take(capacity.coerceAtLeast(0))
