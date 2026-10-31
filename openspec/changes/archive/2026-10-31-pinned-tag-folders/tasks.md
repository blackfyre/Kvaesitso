# Tasks

## 1. Stored membership order (data layer)

- [x] 1.1 Add `ORDER BY id` to `CustomAttrsDao.getItemsWithTag`. Verify that `mise run build` passes and that Room's compile-time query check accepts the query.
- [x] 1.2 Rewrite `CustomAttrsDao.setTags` as a diff in one transaction: delete the item's tag rows whose tag is not in the new list, and insert rows only for tags the item does not have yet. Verify on the device: tag X holds A, B, C; on B's customisation sheet, add tag Y and remove tag Z; the tag editor for X still lists A, B, C.
- [x] 1.3 Add a pure function in `data:customattrs` that orders resolved items by a key list and drops keys without an item. Use it in `CustomAttributesRepositoryImpl.getItemsForTag`. Add `testImplementation(libs.bundles.tests)` to `data/customattrs/build.gradle.kts` and a JVM test covering: the order is kept, an unresolved key is skipped, and duplicate or extra items are ignored. Verify with `mise exec -- ./gradlew :data:customattrs:testDebugUnitTest`.
- [x] 1.4 Add `ORDER BY id` to `BackupRestoreDao.exportCustomAttributes`, and sort the `customizations.*` files by name in `CustomAttributesRepositoryImpl.restore`. Verify on the device: set tag X to C, A, B; back up; change the order to A, B, C; restore; X lists C, A, B.

## 2. Reordering in the tag editor

- [x] 2.1 Add `moveTaggedItem(from, to)` to `EditTagSheetVM`, and a reorderable list of the tagged items (icon and label, `LazyDragAndDropColumn` with `DraggableItem`, height-bounded) on the CustomizeTag page of `EditTagSheet`. Verify on the device: drag C to the top of A, B, C and save; reopening the editor lists C, A, B. Dismissing without saving keeps the old order.
- [x] 2.2 Verify that adding an item through the item picker appends it to the end of the list, and that saving keeps that position.

## 3. Folders in the Favourites grid

- [x] 3.1 In `FavoritesVM`, stop excluding `"tag"` from the manually sorted query of the default view, and keep excluding it from the frequently-used query. Verify on the device: pinned tags appear first in the grid in the Tags section's order; the chip row and the chip-filtered view are unchanged; unpinning a tag removes its folder.
- [x] 3.2 Create `TagFolderVM` (ordered items, custom icon, preview icons for the first four items, `launchFirst` with the same activity options and usage reporting as `SearchableItemVM.launch`) under `app/ui/.../launcher/search/common/grid/folder/`. Verify that `mise run build` passes.
- [x] 3.3 Create `TagFolderIcon` (a custom icon if one is set, otherwise a 2×2 mini-grid in the current icon shape, or an empty folder), and hook it into `GridItem` for `Tag` items. Verify on the device: the preview shows the first four items in stored order and updates after a reorder; a custom tag icon replaces the preview.
- [x] 3.4 Create `TagItemGridPopup` (a header with the tag name and an edit action that opens `showEditTagSheet`, and a `SearchResultGrid` of the items in stored order), and add the `is Tag` branch to `ItemPopup`. Verify on the device: a long-press opens the popup with haptic feedback; tapping an item launches it; the edit action opens the tag editor; back and an outside tap close the popup; a long-press on an item inside the popup behaves correctly, or long-press is disabled there.
- [x] 3.5 Route a tap on a `Tag` grid item to `TagFolderVM.launchFirst`, falling back to opening the popup when nothing launches. Verified on the device: tapping a folder launches its first item. The fallback (no item launches) was checked by code reading only, because exercising it needs a tag whose apps are uninstalled.

## 4. Integration

- [x] 4.1 Verify gesture coexistence on the device: a vertical drag starting on a folder scrolls the widget column and opens nothing; a horizontal swipe starting on a folder has the same result as one starting on another favourite (on the widget page, upstream runs no scaffold action for it) and opens nothing.
- [x] 4.2 Run `mise run build` and `mise run test`, install with `mise run install`, and check that the search screen's favourites also show folders.
- [x] 4.3 Add a line to `CLAUDE.md` stating that tag membership order is the `CustomAttributes` row order (`id`), so upstream merges that touch tag writes or the custom-attribute backup must keep insert order.
