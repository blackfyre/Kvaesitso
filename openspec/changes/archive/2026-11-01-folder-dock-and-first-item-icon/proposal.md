# Proposal

## Why

The owner's home screen shows favourites in the clock widget's dock, not in the Favourites widget, and the dock excludes tags. A pinned folder therefore never reaches the home screen. A folder's icon is either the mini-grid preview or a custom icon. A folder that behaves as "launch the first app, long-press for the rest" reads better when it looks like that first app.

## What Changes

- Pinned tags appear as folders in the clock widget's dock, before the other dock items, in the Favourites editor's Tags order. The dock keeps its size of grid columns × dock rows; folders count towards it. Unpinned tags never appear in the dock. The Favourites widget is unchanged.
- The tag editor gets a per-folder option to use the first item's icon. When it is on, the folder shows the icon of the first item in the tag's stored order and follows reorders. The option is stored as the tag's custom icon. Choosing an icon or an emoji in the icon picker replaces it, and turning it off returns the folder to the mini-grid. Other folders are unaffected.
- This revises project decision 2 ("home screen" means the Favourites widget): the dock is the home screen surface the owner uses.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `favorites-folders`: pinned tags also appear as folders in the clock widget's dock; the folder icon gains a "first item's icon" option alongside the mini-grid and a custom icon.

## Impact

- `app/ui` `FavoritesPartProvider` (upstream file, the dock): a narrow hook that prepends the pinned tags.
- `data:customattrs`: a new custom icon kind for "first item's icon", one line in the upstream `CustomIcon.fromDatabaseEntity`; the class itself goes in a fork-owned file.
- `app/ui` fork-owned files: `TagFolderVM` (icon resolution) and the tag editor's item-order section `EditTagSheetItemOrder.kt` (the option).
- Backup: the option is a custom icon attribute, which backup already exports and restores. The backup format does not change. An upstream build that restores such a backup ignores the unknown icon kind and shows the default tag icon.
- `CLAUDE.md` decision 2 is reworded to name the dock.
