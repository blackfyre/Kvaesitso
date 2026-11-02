## 1. Storage

- [x] 1.1 Add `FolderDock.kt` to `data:customattrs` with the `folder_dock` type constant and `FolderDockRepository` (`docked`, `isDocked`, `setDocked`) on the existing `CustomAttrsDao` queries, and register it in `customAttrsModule`. Verify `mise exec -- ./gradlew :data:customattrs:testDebugUnitTest` builds and passes.
- [ ] 1.2 In `CustomAttributesRepositoryImpl.deleteTag`, clear the tag's icon and `folder_dock` rows (keyed by `Tag(tag).key`) in the existing coroutine. Verify on the device: delete a tag that has a custom icon, recreate it with the same name, and confirm that it shows the mini-grid.

## 2. Dock

- [x] 2.1 Change `pinnedTagsFirst` to take a `docked` lookup. It reads the pinned tags without a limit, keeps the docked ones in order, caps them at `capacity` and fills the rest from `others`. Extract the selection step as a pure function. Add a JVM test in `app/ui/src/test` covering order, capacity, undocked tags skipped, and no docked tags. Verify that the test passes.
- [x] 2.2 In `FavoritesPartProvider`, inject `FolderDockRepository` and pass `it::docked` to `pinnedTagsFirst`. Verify `mise run build` succeeds.
- [x] 2.3 Leave items of docked folders out of the dock's other items: add `FolderDockRepository.itemsIn(tags)`, request `left + excluded` items from `others` and filter them, with the selection in a pure function covered by the JVM test (duplicates removed, freed slot refilled, capacity kept). Verify the test passes and on the device that an app in the docked Home folder no longer appears beside it.

## 3. Tag editor

- [x] 3.1 In `EditTagSheetVM`, load `showInDock` in init. In `save()`:
  - move the option on rename;
  - write it for the new name;
  - skip writing the icon and the option when the tag is being deleted.

  Verify `mise run build` succeeds.
- [ ] 3.2 Add `ShowInDockSwitch` under `FirstItemIconSwitch` in `EditTagSheetItemOrder.kt`, with its label in `strings_fork.xml`. Verify on the device that the switch shows its saved state when the editor is reopened.

## 4. Documentation and verification

- [x] 4.1 Update `CLAUDE.md`:
  - the folders seam note (the dock option, the `deleteTag` lines);
  - the backup paragraph (a new attribute type that older backups restore correctly without does not need a format bump).

  Verify by reading the diff.
- [x] 4.2 Run `mise run test` and `mise run build`. Verify both pass.
- [ ] 4.3 Device check, on the installed debug build, of the spec scenarios:
  - Home with the option on leads the dock;
  - Work, pinned without the option, appears in the grid only;
  - switching the option off removes Home from the dock;
  - renaming Home keeps it in the dock;
  - unpinning removes it from the dock, and re-pinning restores it;
  - a backup made and restored keeps the option.
