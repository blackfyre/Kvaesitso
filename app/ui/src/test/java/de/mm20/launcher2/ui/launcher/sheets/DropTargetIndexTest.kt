package de.mm20.launcher2.ui.launcher.sheets

import org.junit.Assert
import org.junit.Test

class DropTargetIndexTest {
    // Three rows of 100 px: index 0 at 0, index 1 at 100, index 2 at 200.
    private val slots = listOf(Triple(0, 0, 100), Triple(1, 100, 100), Triple(2, 200, 100))

    @Test
    fun centreOverAnotherRowPicksThatRow() {
        Assert.assertEquals(0, dropTargetIndex(40f, slots))
        Assert.assertEquals(2, dropTargetIndex(250f, slots))
    }

    @Test
    fun rowBoundaryBelongsToTheLowerRow() {
        Assert.assertEquals(1, dropTargetIndex(100f, slots))
    }

    @Test
    fun outsideTheListIsNoTarget() {
        Assert.assertNull(dropTargetIndex(-10f, slots))
        Assert.assertNull(dropTargetIndex(300f, slots))
    }
}
