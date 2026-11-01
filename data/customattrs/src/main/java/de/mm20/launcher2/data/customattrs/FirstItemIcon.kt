package de.mm20.launcher2.data.customattrs

import de.mm20.launcher2.ktx.jsonObjectOf

/**
 * Fork (folder-dock-and-first-item-icon): a tag's folder shows the icon of the tag's first item.
 * Stored as the tag's custom icon, so it and any other custom icon exclude each other. Icon
 * providers do not know it: outside folders, the tag keeps its default icon.
 */
data object FirstItemIcon : CustomIcon() {
    internal const val Type = "folder_first_item"

    override fun toDatabaseValue(): String {
        return jsonObjectOf("type" to Type).toString()
    }
}
