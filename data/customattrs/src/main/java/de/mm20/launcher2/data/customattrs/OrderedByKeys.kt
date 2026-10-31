package de.mm20.launcher2.data.customattrs

/**
 * Fork: returns the items in the order of [keys], each at most once. Keys without an item are
 * skipped, and items whose key is not in [keys] are dropped. Used to keep a tag's stored item order
 * after resolving its keys, which loses the order.
 */
internal fun <T> List<T>.orderedByKeys(keys: List<String>, keyOf: (T) -> String): List<T> {
    val byKey = associateBy(keyOf)
    return keys.distinct().mapNotNull { byKey[it] }
}
