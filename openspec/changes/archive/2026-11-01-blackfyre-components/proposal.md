# Proposal

## Why

The Blackfyre theme and depth changes give the launcher the brand's colours, type, shapes and lit panels, but its controls are still Material 3's: rust-filled pill buttons, tonal chips and toggles, Material dialogs. In the brand, controls are physical keys. They have an edge below and a highlight on top, and they sink when pressed. This is the third and last change of decision 1 in `CLAUDE.md`. The owner chose to apply it to every surface, settings included, as a single change.

## What Changes

All of the following apply only while the Blackfyre colour theme is active. With any other theme, every control and surface stays exactly as upstream draws it.

- **Keycap controls.** The Material controls whose look conflicts with the brand are replaced, everywhere, by fork-owned keycaps:
  - The primary key is orange (`bf-accent` with `bf-on-accent`, on a 2 dp `bf-accent-edge`).
  - The secondary key is light (`bf-paper` with `bf-ink`, a 2 dp `bf-line-strong` edge and a 1 dp `bf-highlight` top edge).
  - Both have 10 dp corners (`bf-radius-lg`) and a minimum height of 44 dp. Pressed keys move down by their edge and lose it.
  - Selected chips and checked toggles are drawn as keys held down, in `bf-pressed` with `bf-on-pressed`.
  - Covered: `Button`, `FilledTonalButton`, `OutlinedButton`, `FilledIconButton`, `FilledTonalIconButton`, the three floating action buttons, `FilterChip`, `AssistChip`, `InputChip` and `ToggleButton`.
  - Chips use the `micro` label (10 sp, tracked 0.12 em).
- **Dialogs.** `AlertDialog` gets the panel radius (22 dp) and `bf-shadow-dialog`.
- **Panels.**
  - Launcher cards, popups and the search bar take the panel radius, 22 dp (`bf-radius-panel`), in place of 12 dp.
  - Each settings category becomes a raised panel, under a mono-caps kicker title.
- **Wells and mono numbers.**
  - The calculator's and the unit converter's results sit in wells (`bf-depth-well`), in JetBrains Mono, like the site's stat tiles.
  - The digital clock faces draw their digits in JetBrains Mono.
- **Swapping at call sites.** Each call site switches by its import alone. The fork's composables are signature-compatible with the Material ones they replace.
  - A script rewrites the imports, so it can be rerun after an upstream merge.
  - A JVM test fails when a file still uses a Material control that should be swapped.
  - This edits the imports of about 53 upstream files in `app/ui`.

**Departures from the earlier outline**, which `blackfyre-depth` deferred to this change:
- **Material controls kept.** `TextButton`, the standard `IconButton`, `Switch`, `Slider`, `Checkbox` and `RadioButton` stay Material. They already render in Blackfyre's roles and type: mono-caps accent text, `bf-control-edge` outlines. A key there adds nothing, and leaving them avoids about 30 more upstream files.
- **Plates and the sheen are dropped.**
  - Plates are the brand's dark panels on paper. No launcher surface plays that role, because Blackfyre maps Material surfaces to `bf-surface`.
  - The sheen is for mouse and pen only. The brand gives touch none, so on a phone it would never show.

## Capabilities

### New Capabilities

- `blackfyre-components`: Blackfyre keycap controls, dialogs, panel radius, settings panels, wells and mono numbers, active only with the Blackfyre colour theme, plus the guard that keeps upstream call sites swapped.

### Modified Capabilities

None. The `blackfyre-theme` and `blackfyre-depth` requirements are unchanged: this change reuses the depth carrier and the panel modifier.

## Impact

- **`app:ui`, new fork-owned files:**
  - the keycap composables and their palettes (`ui/component/blackfyre/`), together with `Wells.kt` (the well modifier and its mono style);
  - the import guard test.
- **`app:ui`, upstream edits:**
  - import swaps in about 53 files, plus explicit imports in the files that use `material3.*`;
  - `LauncherCard`: the default shape;
  - `PreferenceCategory`: the panel and the kicker title;
  - `CalculatorItem` and `UnitConverterResults`: wells around the results;
  - `ClockWidget`'s existing `ClockFaceColors` seam: also mono digits.
- **Tooling:** `scripts/blackfyre_controls.py` (the import rewrite) and a `mise` task to run it.
- **`CLAUDE.md`:** the swap mechanism, the guard and the new seams.
- **Merges:** after an upstream merge, the guard test names any new Material call sites; running the script fixes them. An upstream edit near a swapped import conflicts on one line.
- **Not affected:** behaviour, accessibility semantics (roles, states and content descriptions are kept), data, backups, and the media widget.
