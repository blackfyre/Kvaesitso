# Tasks

## 1. Folders in the dock

- [x] 1.1 Add a fork-owned helper next to `TagFolderVM` that returns the pinned tags flow (`includeTypes = ["tag"]`, `minPinnedLevel = AutomaticallySorted`). In `FavoritesPartProvider`, combine it with the existing query (which keeps excluding `"tag"`, with its limit reduced by the tag count) as `tags + others`, truncated to `columns × dockRows`. Verify that `mise run build` passes.
- [x] 1.2 Verify on the device (release build): with the restored backup, the Home folder leads the dock before Termux, Brave and the other pinned items; a tap launches its first item; a long-press opens the folder popup; unpinning Home in the Favourites editor removes it from the dock; the dock holds no more than columns × rows items.

## 2. First item's icon

- [x] 2.1 Add `FirstItemIcon` (`data object`, payload `{"type":"folder_first_item"}`) in a fork-owned file in `data:customattrs`, and the `"folder_first_item" -> FirstItemIcon` line in `CustomIcon.fromDatabaseEntity`. Add a JVM test in `data:customattrs` that a `FirstItemIcon` entity round-trips through `toDatabaseEntity` and `fromDatabaseEntity`. Verify with `mise exec -- ./gradlew :data:customattrs:testDebugUnitTest`.
- [x] 2.2 In `TagFolderVM.customIcon`, resolve `FirstItemIcon` to the icon of the first ordered item (`IconService.getIcon`, full size), or `null` when the tag has no items. Verify that `mise run build` passes.
- [x] 2.3 Add the "Use first item's icon" switch row, showing the first item's icon, to `TaggedItemsOrder`. Bind it to `tagCustomIcon is FirstItemIcon` through `selectIcon(FirstItemIcon)` and `selectIcon(null)`. Put the string in a new `core/i18n/src/main/res/values/strings_fork.xml`. Verify that `mise run build` passes.
- [x] 2.4 Verify on the device (release build), against each scenario of the modified folder-icon requirement: turning the option on shows the first item's icon in the dock and in the Favourites grid; a reorder updates it; picking an emoji replaces it and the switch reads off; turning it off returns the mini-grid; other folders are unaffected; after a backup and restore, the option is still on.

## 3. Project record and checks

- [x] 3.1 Reword `CLAUDE.md` decision 2 to cover the clock widget's dock, and list the new upstream seams (`FavoritesPartProvider`, `CustomIcon.fromDatabaseEntity`) where `CLAUDE.md` describes them. Verify by reading the diff.
- [x] 3.2 Run `mise run test` and `mise exec -- ./gradlew :app:app:assembleDefaultRelease`, and install the release build on the device. Verify that all tests pass, the build succeeds, and the launcher starts as the home app without a crash in `adb logcat -b crash`.
