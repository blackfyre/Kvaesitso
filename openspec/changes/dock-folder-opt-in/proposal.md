# Proposal

## Why

The clock widget's dock shows every pinned tag as a folder (`favorites-folders`, from `folder-dock-and-first-item-icon`). Pinning a tag puts it in the Favourites grid, but the dock is small. The owner wants to choose which folders reach the dock, not have every pinned folder take a dock slot.

## What Changes

- The tag editor gets a per-folder option, "Show in dock", next to "Use first item's icon". It is off by default.
- The dock shows a tag as a folder only when the tag is pinned **and** its option is on. Order and capacity are unchanged: such folders lead the dock in the Favourites editor's Tags order and count towards grid columns × dock rows.
- An app in a folder shown in the dock no longer also appears in the dock on its own (as a frequently used or pinned item); the next item fills the slot.
- Unpinning a tag keeps its option, so pinning it again brings the folder back to the dock.
- The option follows a rename of the tag and survives backup and restore. Restoring a backup made before this change leaves every tag's option off.
- Deleting a tag clears its option and its custom icon, so a later tag with the same name starts clean. Today the custom icon is left behind on delete (upstream behaviour), and the new tag inherits it. This fixes the icon case as well; it is a small addition beyond the dock option.
- The Favourites grid is unchanged: every pinned tag remains a folder there.
- **BREAKING** (for the owner's current setup): existing folders leave the dock until their option is turned on.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `favorites-folders`: the dock shows only pinned tags whose "Show in dock" option is on; the tag editor gains that option; deleting a tag clears its folder settings.

## Impact

- `data:customattrs`: a fork-owned file for the option's storage (a custom-attribute row on the tag's key) and one Koin line in the upstream `Module.kt`. Two lines in the upstream `CustomAttributesRepositoryImpl.deleteTag` clear the tag's icon and option.
- `app/ui` fork-owned files: `PinnedTagsFirst.kt` (dock filter) and `EditTagSheetItemOrder.kt` (the switch).
- `app/ui` upstream files: `FavoritesPartProvider` (passes the option into the dock query) and `EditTagSheetVM` (loads and saves the option, moves it on rename).
- `core:i18n` `strings_fork.xml`: the switch's label.
- Backup: the option is a new row type in the existing `customizations.*` files. The file structure and the backup format version do not change (see design).
- `CLAUDE.md`: the dock seam note mentions the option.
