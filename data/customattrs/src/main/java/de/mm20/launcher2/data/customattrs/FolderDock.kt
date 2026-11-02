package de.mm20.launcher2.data.customattrs

import de.mm20.launcher2.database.AppDatabase
import de.mm20.launcher2.database.entities.CustomAttributeEntity
import de.mm20.launcher2.search.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Fork (dock-folder-opt-in): a pinned tag's folder is shown in the clock widget's dock only when
 * its "Show in dock" option is on. Stored as a custom-attribute row on the tag's key
 * (`tag://<name>`): a row means on, no row means off. Not a [CustomAttributeType]; nothing reads
 * these rows through [CustomAttribute.fromDatabaseEntity]. Backup carries the rows with the
 * other custom attributes.
 */
class FolderDockRepository internal constructor(private val appDatabase: AppDatabase) {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    /** The names among [tags] whose option is on. */
    fun docked(tags: List<String>): Flow<Set<String>> {
        if (tags.isEmpty()) return flowOf(emptySet())
        val keys = tags.associateBy { Tag(it).key }
        return appDatabase.customAttrsDao().getCustomAttributes(keys.keys.toList(), Type)
            .map { rows -> rows.mapNotNullTo(mutableSetOf()) { keys[it.key] } }
    }

    /** The keys of the items in [tags]. */
    fun itemsIn(tags: List<String>): Flow<Set<String>> {
        if (tags.isEmpty()) return flowOf(emptySet())
        val dao = appDatabase.customAttrsDao()
        return combine(tags.map { dao.getItemsWithTag(it) }) { lists -> lists.flatMapTo(mutableSetOf()) { it } }
    }

    suspend fun isDocked(tag: String): Boolean = docked(listOf(tag)).first().isNotEmpty()

    fun setDocked(tag: String, docked: Boolean) {
        scope.launch { write(tag, docked) }
    }

    /** Moves the option from [oldName] to [newName] (a renamed tag). */
    fun move(oldName: String, newName: String, docked: Boolean) {
        scope.launch {
            write(oldName, false)
            write(newName, docked)
        }
    }

    private fun write(tag: String, docked: Boolean) {
        val dao = appDatabase.customAttrsDao()
        val key = Tag(tag).key
        dao.clearCustomAttribute(key, Type)
        if (docked) dao.setCustomAttribute(CustomAttributeEntity(id = null, type = Type, value = "1", key = key))
    }

    companion object {
        const val Type = "folder_dock"
    }
}
