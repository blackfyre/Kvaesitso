# Proposal

## Why

The fork needs home-screen app folders. Kvaesitso already lets a tag be pinned, but a pinned tag only shows as a filter chip above the Favourites grid: there is no folder icon, a tap filters rather than launches, and tag membership has no defined order, so "the first item" of a tag is undefined. Building folders on pinned tags avoids a new folder entity and keeps the diff against upstream small.

## What Changes

- Pinned tags appear as folder icons in the Favourites grid (the widget and the search screen's favourites), before the other favourites, in the order the Favourites editor's Tags section already defines. The tag chip row above the grid stays unchanged.
- A tap on a folder launches the first item of the tag's stored order. A tag with no launchable item opens the folder instead.
- A long-press on a folder opens a folder popup from the icon: the tag's items as a grid in stored order, plus an edit action that opens the existing tag editor.
- A folder shows a mini-grid preview of its first items instead of the tag glyph, unless the user has set a custom icon for the tag.
- A swipe on a folder opens it, only if the gesture can be told apart from widget-column scrolling and the scaffold gestures. Otherwise it is left out and reported.
- Tag membership gets a persistent, user-editable order:
  - The tag editor lists the tag's items in order and lets the user drag them into a new order.
  - Editing an item's tags no longer moves the item to the end of its other tags.
  - Backup and restore preserve the order. The backup format does not change.

## Capabilities

### New Capabilities

- `tag-membership-order`: the stored order of a tag's items, how it is read, edited in the tag editor, kept stable by other tag edits, and preserved by backup and restore.
- `favorites-folders`: pinned tags shown as folders in the Favourites grid: placement, preview icon, tap-to-launch, the folder popup and the optional swipe.

### Modified Capabilities

None. The project has no existing specs.

## Impact

- `data:database`: `CustomAttrsDao` (ordered membership query, order-preserving `setTags`) and `BackupRestoreDao` (ordered export). No schema change and no Room migration.
- `data:customattrs`: `CustomAttributesRepository` (keep the stored order when resolving keys; restore files in order).
- `app:ui`: `FavoritesVM` (include pinned tags in the default grid), `GridItem` (folder icon, tap, popup branch), the tag editor (`EditTagSheet`, `EditTagSheetVM`), and new folder files (preview icon, popup, launch helper).
- Any other place that shows a `Tag` through `GridItem` would also get the folder preview and tap-to-launch. Upstream search does not list tags as results (a tag search lists the tagged items), so in practice this affects only the Favourites grid and the search screen's favourites.
- Not affected: the chip row, the clock widget's favourites part (it keeps excluding tags), the backup format version, the Room schema.
