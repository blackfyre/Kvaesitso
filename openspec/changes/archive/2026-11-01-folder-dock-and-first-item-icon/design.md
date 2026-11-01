# Design

## Context

- **Dock.** The clock widget's dock is `FavoritesPartProvider` (`app/ui/.../widgets/clock/parts/`, upstream). It shows one `SearchResultGrid` without labels, filled by one `FavoritesService.getFavorites` query with `minPinnedLevel = FrequentlyUsed`, `excludeTypes` containing `"tag"`, and `limit = columns × dockRows`. `GridItem` already draws and handles any `Tag` as a folder (`FolderAwareLauncherIcon`, `rememberTagFolderLaunch`, `TagItemGridPopup`), so the dock only lacks the data.
- **Pinned tags.** Pinned tags carry the highest pin positions, in the Favourites editor's Tags order (see the archived `pinned-tag-folders` design). `FavoritesVM.pinnedTags` gets them with `includeTypes = ["tag"]` and `minPinnedLevel = AutomaticallySorted`.
- **Tag icons.** A tag's icon is one custom attribute of type `icon` whose value is a JSON payload with a `type` discriminator, parsed by `CustomIcon.fromDatabaseEntity` (`data:customattrs`, upstream). An unknown `type` parses to `null`. `IconService.resolveCustomIcon` falls back to the normal providers for a custom icon kind it has no provider for. The tag editor keeps the pending icon in `EditTagSheetVM.tagCustomIcon` and writes it on save. The icon picker (`PickIcon`: icon tab, emoji tab, and "default icon", which selects `null`) sets it through `selectIcon`.
- **Backup.** `CustomAttributesRepository` exports and restores every custom attribute row with its raw type and value, so a new icon payload needs no backup change.

## Goals / Non-Goals

**Goals:**

- Folders in the dock with no change to how pins are stored or ordered.
- The "first item's icon" option with no change to the database schema or the backup format, and with edits to upstream files limited to single-line hooks.

**Non-Goals:**

- Folders in the dock's frequently used items. Only pinned tags appear.
- Changing the icon shown for a tag in the tag chips, search results or the tag editor's own preview circle. These keep showing the default tag icon when the option is on.
- A global setting for the folder icon style.

## Decisions

### 1. Dock: prepend pinned tags in a separate query

`FavoritesPartProvider` combines two flows: the pinned tags (`includeTypes = ["tag"]`, `minPinnedLevel = AutomaticallySorted`) and its existing query, which keeps excluding `"tag"`. The result is `tags + others`, truncated to `columns × dockRows`. The existing query's limit is reduced by the number of tags, so the dock never fetches more than it shows.

- **Alternative: drop `"tag"` from the existing exclusion.** The query also returns frequently used items (`pinPosition = 0`, `launchCount > 0`). A tag that was once launched or pinned and later unpinned could then appear. A folder launch records the launch against the item, not the tag, so this is unlikely. However, the spec requires that only pinned tags appear, and a separate query guarantees it.
- The hook lives in the upstream file because the provider builds its query inline. The edit is confined to the `favorites` flow. The pinned-tag flow is a small fork-owned helper next to `TagFolderVM`, so the upstream file gains one call and a comment.

### 2. "First item's icon" as a new `CustomIcon` kind

A fork-owned file in `data:customattrs` declares `data object FirstItemIcon : CustomIcon()` with payload `{"type":"folder_first_item"}`. Kotlin allows subclasses of a sealed class in another file of the same package and module. `CustomIcon.fromDatabaseEntity` gains one line: `"folder_first_item" -> FirstItemIcon`.

- Storing it as the tag's custom icon makes the option and a custom icon mutually exclusive by construction, which is what the spec asks for. It also reuses the editor's pending-icon state, its save path, and backup.
- Elsewhere the kind resolves like an unknown kind: `IconService` has no provider for it and falls back to the default tag icon. That is the intended look for chips and search results, and it needs no `IconService` change.
- **Alternative: a separate custom attribute type (for example `folder_icon`).** It needs a new `CustomAttributeType` entry and repository methods, in upstream files. Keeping the option and a custom icon exclusive would also require logic in two places.
- **Alternative: a preference keyed by tag name.** It would not survive tag renames without extra code, and it is not covered by the custom-attribute backup.

### 3. Folder icon resolution in `TagFolderVM`

`TagFolderVM.customIcon` checks the raw custom icon:

- `FirstItemIcon`: emit the icon of `items.first()` through `IconService.getIcon`, at the full folder size, or `null` when there are no items. `null` makes `FolderAwareLauncherIcon` draw the preview, which shows an empty folder for a tag with no items, as the spec requires.
- Any other non-null kind: resolve it as today.
- `null`: emit `null`, giving the mini-grid.

Because `items` is the ordered, resolvable item list, the icon follows reorders and skips items that no longer resolve.

### 4. Editor option in the fork-owned item-order section

`TaggedItemsOrder` (`EditTagSheetItemOrder.kt`, fork-owned) gets a switch row above the list: "Use first item's icon", showing the first item's icon. Its checked state is `tagCustomIcon.value is FirstItemIcon`. Turning it on selects `FirstItemIcon`, and turning it off selects `null`, using the existing `selectIcon`. Picking an icon or emoji in `PickIcon` replaces it, because both write the same state.

- The new string goes in a new fork-owned resource file, `core/i18n/src/main/res/values/strings_fork.xml`, rather than in upstream's `strings.xml`, to avoid merge conflicts.
- **Alternative: a third tab in `PickIcon`.** That edits the upstream editor layout and reads as an icon choice, when the option is about the folder's behaviour.

### 5. Project decision 2

Reword `CLAUDE.md` decision 2: "home screen" covers the Favourites widget and the clock widget's dock.

## Risks / Trade-offs

- [The upstream `CustomIcon.fromDatabaseEntity` changes in a merge] → The added line sits in a `when` over string literals. A conflict would be local and obvious. `FirstItemIcon` itself is in a fork file.
- [An upstream `when` over `CustomIcon` becomes exhaustive without `else`] → The build fails at that `when`, so the problem is caught at compile time.
- [Upstream builds restoring a fork backup] → `fromDatabaseEntity` returns `null` for the unknown kind, so the tag shows its default icon. No data is lost.
- [The tag editor's preview circle shows the tag glyph while the option is on] → This is accepted; the switch row shows the first item's icon.
- [R8 release builds] → No reflection is added. `FirstItemIcon` is referenced directly.
