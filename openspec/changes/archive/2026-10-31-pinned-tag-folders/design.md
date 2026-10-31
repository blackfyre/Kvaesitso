# Design

## Context

See `proposal.md` for motivation and `specs/` for the required behaviour. The state of upstream `main` (`1aaa058`) that shapes the approach:

- **Tag storage.** A tag membership is a row in `CustomAttributes` (`type = 'tag'`, `key` = item key, `value` = tag name). The table's `id` is `INTEGER PRIMARY KEY AUTOINCREMENT` (schema 34).
- **Writes.** `CustomAttrsDao.setItemsWithTag` (the tag editor's save) deletes the tag's rows and reinserts them in list order. `addTag` deletes and reinserts one row, which puts the item at the end. `setTags` (an item's customisation sheet) deletes all of the item's tag rows and reinserts them, which moves the item to the end of every tag it belongs to. `renameTag` updates rows in place.
- **Reads.** `getItemsWithTag` has no `ORDER BY`. `CustomAttributesRepositoryImpl.getItemsForTag` passes the keys to `SavableSearchableRepository.getByKeys`, an `IN (…)` query that returns rows in the `Searchable` table's order. The order of the key list is lost there.
- **Backup.** `BackupRestoreDao.exportCustomAttributes` pages through the table with `LIMIT/OFFSET` and no `ORDER BY`. Restore wipes the table and imports `customizations.NNNN` files in `File.listFiles()` order, which is unspecified.
- **Favourites.** Pinned tags are `Searchable` rows whose pin position puts them at the head of the manually sorted favourites. `EditFavoritesSheetVM.save()` writes the Tags section's order first. `FavoritesVM` excludes `"tag"` from the default grid and shows pinned tags only as chips (`FavoritesTagSelector`). With a chip selected, the grid shows `getItemsForTag(tag)` sorted by item.
- **Grid items.** `GridItem` launches on tap unless `preferDetailsOverLaunch` is set (`Tag` sets it), and otherwise opens `ItemPopup`. `ItemPopup` has no `Tag` branch. `Tag.launch()` returns `false`, and `core:base` cannot reach the repositories.
- **Gestures.** The scaffold has configurable `swipeUp`, `swipeDown`, `swipeLeft` and `swipeRight` actions (`GestureSettings`), and vertical drags in the home widget column scroll it.

## Goals / Non-Goals

**Goals:**

- Keep the upstream diff small and localised. New behaviour goes into new files, and upstream files get narrow hooks.
- No schema change and no change to the backup format.

**Non-Goals:**

- Free placement of folders among other favourites. Folders sit at the head of the grid, in the Tags section's order (see Decisions).
- Folder badges (notification counts aggregated from members).
- Nested folders. A tag inside a tag is shown as an item, and tapping it in the popup opens that folder's popup.
- Changing the chip row, the chip filter view's alphabetical sort, or the clock widget's favourites part.
- A swipe gesture to open a folder (see Decisions).

## Decisions

### 1. Store membership order as row order, with no new column

Order is the ascending `id` of a tag's `CustomAttributes` rows. `AUTOINCREMENT` guarantees that a new row gets a larger `id` than any existing one, so every insert appends. The tag editor's save already rewrites the rows in list order, and `addTag` already appends.

- **Alternative: a `position` column, as the brief assumed.** That needs a Room migration (34 → 35), a new schema JSON, changes to every insert path and to backup serialisation, and a backup format bump so older builds ignore the field. It is a larger diff in upstream files that merge often, and it adds nothing the row order cannot express.
- **Consequence:** the only fixes needed are the code paths that destroy or ignore row order:
  - `getItemsWithTag` gets `ORDER BY id`.
  - `CustomAttributesRepositoryImpl.getItemsForTag` reorders the resolved items to match the key list, and drops unresolved keys. This is a small pure function with a JVM unit test.
  - `CustomAttrsDao.setTags` becomes a diff. It deletes the item's rows for tags no longer in the list and inserts rows only for tags that are new to the item. An existing membership keeps its row, so it keeps its position, and a new one appends.
  - `exportCustomAttributes` gets `ORDER BY id`, so paging is deterministic and the files list entries in order.
  - Restore sorts the `customizations.NNNN` files by name before importing. Rows are imported with `id = null` in file order, so the new ids reproduce the order. Older backups restore in their entry order, which is the order the spec requires.

### 2. Folders are pinned tags placed at the head of the default grid

`FavoritesVM` stops excluding `"tag"` from the manually sorted part of the default view. Pinned tags already carry the highest pin positions, in the Tags section's order, so they come out first with no change to the Favourites editor or to how pins are saved. The frequently-used query keeps excluding tags. The chip-filtered view is unchanged.

- **Alternative: let tags be dragged among the other favourites in the editor.** The editor keeps tags in a separate chip section and saves them ahead of the manual items. Mixing them in means reworking `buildItemList`, `moveItem` and `save`, plus the Tags section UI. That is a large edit to one of the most actively changed upstream files. It can follow later as its own change if fixed head placement proves too limiting.
- `SearchFavoritesVM` (the search screen's favourites) inherits the change and shows folders too. This is intended: it is the same Favourites list.

### 3. Folder UI lives in new files, and `GridItem` gets three hooks

New files under `app/ui/.../launcher/search/common/grid/folder/`:

- `TagFolderVM`: for a `Tag`, exposes the ordered items (`TagsService.getTaggedItems`), the tag's custom icon (`IconService.getCustomIcon`), and the preview icons for the first four items. Its `launchFirst(context, bounds)` tries the items in order until one launches, using the same activity options and usage reporting as `SearchableItemVM.launch`.
- `TagFolderIcon`: a composable that draws, inside the current icon shape and size, either the custom icon (through `ShapedLauncherIcon`) or a 2×2 mini-grid of the members' icons on a surface-variant background. An empty folder shows the background alone.
- `TagItemGridPopup`: the popup content for a tag. It has a header with the tag's name and an edit button (`LocalBottomSheetManager.showEditTagSheet`) and a `SearchResultGrid` of the items in stored order. It follows the existing `*ItemGridPopup` signature (`show`, `animationProgress`, `origin`, `onDismiss`).

Hooks in `GridItem.kt`:

1. **Icon:** `if (item is Tag) TagFolderIcon(…) else ShapedLauncherIcon(…)`.
2. **Tap:** for a `Tag`, the tap calls `TagFolderVM.launchFirst`, and if nothing launches it opens the popup. `preferDetailsOverLaunch` stays `true` on `Tag`, so other call sites are unaffected.
3. **Popup:** one `is Tag -> TagItemGridPopup(…)` branch in `ItemPopup`.

The long-press path is unchanged (haptic feedback, then the popup).

- **Alternative: render the preview in `IconService` as a new `LauncherIconLayer`.** That spreads the change across `core:base` (layer type), `services:icons` (provider) and the icon renderer, and the cache would need invalidating when membership changes. A composable is local to the UI module and recomposes from flows.
- **Alternative: make `Tag.launch` launch the first item.** `core:base` has no access to the tag or searchable repositories. Injecting them there would invert the module layering.
- **Consequence:** any `Tag` shown through `GridItem` gets the preview and tap-to-launch. Upstream search does not list tags as results, so this is limited to the Favourites grids.

### 4. Reordering in the tag editor uses the existing drag-and-drop list

`EditTagSheet`'s CustomizeTag page gets a reorderable list of `taggedItems` (icon and label rows) below the item-picker button. It uses `LazyDragAndDropColumn` and `DraggableItem` as `ConfigureFavoritesWidget` already does, inside a height-bounded container. `EditTagSheetVM` gets `moveTaggedItem(from, to)`. Save is unchanged: `updateTag` → `setItemsForTag` already writes the list order. Items added in the picker append, which matches the spec.

- **Alternative: reorder inside the item picker.** The picker is an alphabetical, checkable app list. Mixing selection and ordering in it would be confusing, and the picker is a larger upstream composable.

### 5. No swipe to open

Every swipe direction is already taken: up and down scroll the widget column and can be bound to scaffold gestures, and left and right can be bound to scaffold gestures too. A folder-specific swipe would have to win over one of these, which the spec forbids, and its meaning would change with the user's gesture settings. The swipe is therefore left out, as the brief allows. Opening by long-press covers the need.

## Risks / Trade-offs

- [Row order depends on `AUTOINCREMENT` and on every insert path appending] → The DAO is the only writer. The `setTags` diff and an ordered export/restore close the known gaps, and a unit test pins the read-side reordering. Any future upstream write path that deletes and reinserts rows would reorder silently. This is noted in `CLAUDE.md` so it is checked during upstream merges.
- [The tag editor's save rewrites all of the tag's rows, even when nothing changed] → Row ids grow faster. With a 32-bit signed `id` space this is not a practical limit.
- [Upstream merge conflicts in `CustomAttrsDao`, `BackupRestoreDao`, `CustomAttributesRepository`, `FavoritesVM`, `GridItem`, `EditTagSheet(VM)`] → Each edit is a few lines at a stable seam. The folder UI is in new files.
- [Popup inside popup: a long-press on an item inside the folder popup opens that item's own `ItemPopup` over the folder] → `Overlay` stacks. This is checked on the device, and if it misbehaves, long-press is disabled inside the folder grid.
- [Folders count toward the default grid's `10 × columns` pinned limit] → A user with many pinned tags sees fewer other favourites. This is accepted.
- [The preview resolves up to four icons per folder on every recomposition source change] → It uses the same cached `IconService.getIcon` flows as the grid items.

## Migration Plan

- No data migration. Existing memberships keep their current row order, which becomes their stored order.
- Rollback: reverting the change leaves the data valid for upstream code, because there is no schema or format change.
