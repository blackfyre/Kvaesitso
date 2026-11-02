## Context

- The dock (`FavoritesPartProvider`, upstream) gets its items from the fork's `FavoritesService.pinnedTagsFirst(capacity) { others }` (`PinnedTagsFirst.kt`). It takes every tag pinned at `AutomaticallySorted` or above, then fills the remaining slots from `others`.
- Per-tag folder settings already live in `CustomAttributes` rows keyed by the tag's own key (`Tag(name).key`, `tag://<name>`): the custom icon, including the fork's `FirstItemIcon`.
- Renaming a tag (`TagsServiceImpl.updateTag`) moves the `tag` rows and the `Searchable` row, but not rows keyed by the tag's key. The tag editor moves the icon itself: `EditTagSheetVM.save()` clears `Tag(oldName)`'s icon and writes `Tag(newName)`'s.
- Deleting a tag (`TagsServiceImpl.deleteTag`) removes the `Searchable` row and the `tag` rows (`CustomAttrsDao.deleteTag`), but not the icon row. `cleanUp()` removes only orphaned `tag` and `label` rows.
- Backup exports every `CustomAttributes` row (`ORDER BY id`) with its `type` string. Restore reinserts them as they are. `CustomAttribute.fromDatabaseEntity` logs and returns null for an unknown type, but it is only called on rows read by a known type.

## Goals / Non-Goals

**Goals:**
- Store the option where the tag's other folder settings are stored, so rename, delete and backup treat them alike.
- Keep the upstream edits to a few lines.

**Non-Goals:**
- No change to the Favourites grid, the Favourites editor or how tags are pinned.
- No migration that turns the option on for currently pinned tags.
- No fix for leftover custom icons from tags deleted before this change. `cleanUp()` stays as it is.

## Decisions

1. **Storage: a fork-owned custom-attribute type, `folder_dock`.** A row `(key = tag://<name>, type = "folder_dock", value = "1")` means "on"; no row means "off". A new fork file, `FolderDock.kt` in `data:customattrs`, holds the type constant and a small `FolderDockRepository`:
   - `docked(tags: List<String>): Flow<Set<String>>`, the names among `tags` that have the row, built on the existing `CustomAttrsDao.getCustomAttributes(keys, type)`;
   - `isDocked(tag)`;
   - `setDocked(tag, on)`, built on `clearCustomAttribute` and `setCustomAttribute`.

   It is registered with one `factory` line in `customAttrsModule`. The type is not added to the upstream `CustomAttributeType` enum: nothing reads these rows through `fromDatabaseEntity`.
   *Alternatives:*
   - A preference holding a set of tag names. Rejected because it needs an edit to the upstream `LauncherSettingsData`, a second home for per-tag state, and its own rename and delete handling.
   - Encoding the option in the tag's `PinnedLevel`. Rejected because it is invisible and fragile.

2. **Dock filter in `pinnedTagsFirst`.** The function takes a `docked: (List<String>) -> Flow<Set<String>>` parameter.
   - It reads the pinned tags without a limit, keeps those in `docked`, preserves their order and takes up to `capacity`.
   - It then fills the remaining slots from `others`, as now.
   - `FavoritesPartProvider` injects `FolderDockRepository` and passes `it::docked`. That is one extra line in the upstream file beside the existing hook.
   - The filtering step is a pure function, so it can be unit-tested in `app/ui/src/test` without Room.
   - Reading without a limit is needed because undocked tags must not use up the capacity. The pinned-tag list is small.

3. **Editor.**
   - `EditTagSheetVM` loads the option in its init, next to `tagCustomIcon`, into a `showInDock` state.
   - `save()` writes the option the way it writes the icon. On rename it clears `oldName`'s row. It then sets `newName`'s row to the current value.
   - When `save()` deletes the tag, because its items or its name are empty, it writes neither the icon nor the option. Today `save()` re-writes the icon for `newName` even on that path, which would undo the clearing in decision 4.
   - The switch, `ShowInDockSwitch`, sits in `EditTagSheetItemOrder.kt` under `FirstItemIconSwitch`, with the same layout. Its label is a new string in `strings_fork.xml`.

4. **Delete clears folder settings.** `CustomAttributesRepositoryImpl.deleteTag` is upstream code; it gains two `clearCustomAttribute` calls inside its existing coroutine: one for the icon type and one for `folder_dock`, both keyed by `Tag(tag).key`. This covers both delete paths, the editor and Settings › Tags, because both go through `TagsService.deleteTag`.

5. **No backup-format bump.** `CLAUDE.md` asks for a minor bump when what a component serialises changes. The rule exists for changes that make older backups restore *wrongly*, such as tag order. Here the `customizations.*` file structure is unchanged, the new type is one more value of an existing field, and an older backup restores correctly with the option off.
   - The format number is shared with upstream. A fork bump would mark every stock-Kvaesitso backup as "partially compatible" and would collide with upstream's next bump.
   - The precedent: `FirstItemIcon` added a new stored value without a bump.
   - `CLAUDE.md`'s backup paragraph is reworded to state this distinction.

## Risks / Trade-offs

- [Existing folders leave the dock] → Expected and stated in the proposal. Turn the option on for Home after installing.
- [An upstream merge adds a `folder_dock` type or changes `deleteTag`] → Unlikely. The fork's type name is prefixed like `folder_first_item`. The `deleteTag` lines are listed in the `CLAUDE.md` seams.
- [Upstream builds restoring a fork backup] → They reinsert `folder_dock` rows they never read. Harmless.
- [The `docked` flow is recomputed when the pinned tags change] → It is one query over a handful of keys.

## Migration Plan

None in code. After installing, turn "Show in dock" on for each folder that should stay in the dock. Rollback is reverting the commit. Leftover `folder_dock` rows are then ignored.
