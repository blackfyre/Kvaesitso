package de.mm20.launcher2.data.customattrs

import org.junit.Assert
import org.junit.Test

class OrderedByKeysTest {

    private data class Item(val key: String)

    private fun order(keys: List<String>, items: List<Item>): List<String> =
        items.orderedByKeys(keys) { it.key }.map { it.key }

    @Test
    fun keepsKeyOrderNotItemOrder() {
        val items = listOf(Item("a"), Item("b"), Item("c"))
        Assert.assertEquals(listOf("c", "a", "b"), order(listOf("c", "a", "b"), items))
    }

    @Test
    fun skipsKeysWithoutItem() {
        val items = listOf(Item("c"), Item("a"))
        Assert.assertEquals(listOf("a", "c"), order(listOf("a", "b", "c"), items))
    }

    @Test
    fun dropsItemsNotInKeysAndRepeatsNothing() {
        val items = listOf(Item("a"), Item("x"), Item("b"))
        Assert.assertEquals(listOf("b", "a"), order(listOf("b", "a", "b"), items))
    }
}
