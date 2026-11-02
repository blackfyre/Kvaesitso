package de.mm20.launcher2.ui.launcher.search.common.grid.folder

import de.mm20.launcher2.search.Tag
import org.junit.Assert
import org.junit.Test

class DockedFoldersTest {
    private val pinned = listOf(Tag("Home"), Tag("Work"), Tag("Games"))

    @Test
    fun keepsOnlyDockedTagsInPinnedOrder() {
        Assert.assertEquals(
            listOf(Tag("Home"), Tag("Games")),
            dockedFolders(pinned, setOf("Games", "Home"), 5),
        )
    }

    @Test
    fun undockedTagsDoNotUseCapacity() {
        Assert.assertEquals(
            listOf(Tag("Work"), Tag("Games")),
            dockedFolders(pinned, setOf("Work", "Games"), 2),
        )
        Assert.assertEquals(listOf(Tag("Work")), dockedFolders(pinned, setOf("Work", "Games"), 1))
    }

    @Test
    fun noDockedTagsGivesNoFolders() {
        Assert.assertEquals(emptyList<Tag>(), dockedFolders(pinned, emptySet(), 5))
    }

    // Tags stand in for apps here: dockItems only compares keys.
    private val home = Tag("Home")
    private val reolink = Tag("Reolink")
    private val termux = Tag("Termux")
    private val brave = Tag("Brave")

    @Test
    fun folderItemsAreNotRepeatedAndTheNextItemFillsTheSlot() {
        Assert.assertEquals(
            listOf(home, termux, brave),
            dockItems(listOf(home), listOf(reolink, termux, brave), setOf(reolink.key), 3),
        )
    }

    @Test
    fun dockItemsKeepCapacity() {
        Assert.assertEquals(
            listOf(home, termux),
            dockItems(listOf(home), listOf(termux, brave), emptySet(), 2),
        )
    }
}
