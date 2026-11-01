# Tasks

## 1. Inventory and keys

- [x] 1.1 List every Material 3 overload of the swapped controls that `app/ui` calls (named arguments and trailing lambdas included), from the Material 3 1.5.0-beta02 sources. Record the list in this task. Verify that each call site in `grep` output maps to one recorded overload.

  Inventory (2026-11-01), from call-site argument names, with all positional arguments in trailing lambdas:
  - `Button`: the `shape` overload (34 sites).
  - `FilledTonalButton`: `shape` (9 sites), `shapes` (1 site).
  - `OutlinedButton`: `shape` (16 sites; 2 pass `colors`).
  - `FilledIconButton` (2) and `FilledTonalIconButton` (13): `shape`.
  - `FloatingActionButton` (2) and `SmallFloatingActionButton` (1, passes `elevation`).
  - `FilterChip`: `shape` (20 sites).
  - `AssistChip` (4; 1 passes `colors` and `border`).
  - `InputChip`: `shape` (3; 1 passes `colors`).
  - `ToggleButton`: the single overload, always with `shapes` (20).
  - `AlertDialog`: the slot overload (15).
  - `ExtendedFloatingActionButton`: no call sites, so it is neither implemented nor swapped.
- [x] 1.2 Add `KeyPalettes.kt`: Primary, Secondary (Paper and Dark), Pressed (Paper and Dark) and Custom, with `darken`, using the design's source values. Verify with a JVM test that each palette matches the token values.
- [x] 1.3 Add `Key.kt`: face, edge, highlight, the pressed and held-down looks, reduced motion, semantics and the minimum touch target. Verify by building, and with the device checks in 6.2.

## 2. Controls

- [x] 2.1 Add `Buttons.kt`: `Button`, `FilledTonalButton`, `OutlinedButton`, `FilledIconButton`, `FilledTonalIconButton` and the three floating action buttons, each overload from 1.1, delegating to Material when `LocalBlackfyreDepth` is `null`. Verify by building.
- [x] 2.2 Add `Chips.kt` (`FilterChip`, `AssistChip`, `InputChip`) and the `micro` style, and `Toggles.kt` (`ToggleButton`), with selected or checked as a held-down key. Verify by building, and with a JVM test that `micro` is 10 sp with 0.12 em tracking.
- [x] 2.3 Add `Dialogs.kt` (`AlertDialog`) with the 22 dp shape, the `surface` container and the dialog shadow. Verify by building.

## 3. Swap and guard

- [x] 3.1 Add `scripts/blackfyre_controls.py` (with `--revert`) and a `mise` task `controls`. Verify that running it twice changes nothing the second time, and that `--revert` then the forward run gives a clean `git diff` of the swap. Done: the second run changed 0 files, and revert-then-swap gave an identical diff (54 files).
- [x] 3.2 Add `BlackfyreControlImportsTest`. Verify that it fails before the swap and names the files.
- [x] 3.3 Run the script over `app/ui`. Verify that the guard test passes and `mise run build` compiles.

## 4. Surfaces

- [x] 4.1 Add `blackfyreShape` and `blackfyreKicker`. Apply `blackfyreShape` as `LauncherCard`'s default shape. Verify on the device that search results, the search bar and popups have 22 dp corners.
- [x] 4.2 `PreferenceCategory`: the panel and the kicker title. Verify on the device in Appearance settings, in Paper and Dark.
- [x] 4.3 Add `Wells.kt` (`blackfyreWell` and the mono content style), and apply it in the calculator and unit converter results. Verify on the device with `12*7` and `100 km`: wells in both themes, and still while the device tilts.
- [x] 4.4 `ClockFaceColors`: mono display styles with `tnum`. Verify on the device on a digital clock face; the width stays stable across a minute change.

  Progress: implemented. Not yet checked on the device: the owner's clock uses the segment face, and switching it was left to the owner. The owner then checked it on the device ("everything looks good").

## 5. Theme off

- [x] 5.1 Verify that with a non-Blackfyre colour theme, buttons, chips, toggles, dialogs, cards, settings categories, wells and clock digits render as upstream (screenshots compared with `main`'s build). Restore the owner's theme afterwards.

## 6. Checks and documentation

- [x] 6.1 Run `mise run test` and `mise run build`; all tests pass.
- [x] 6.2 Device checks on the Pixel 7 (release build), in Paper and Dark:
  - the press-and-release sink;
  - a disabled key;
  - selected filter chips in search;
  - toggle-button groups in settings;
  - a confirmation dialog;
  - TalkBack announcing a chip's selected state;
  - the dense rows (search filters, the tag editor) for crowding from the 44 dp keys.

  Tune the custom-edge `darken` factor here, and record it.

  Progress (2026-11-01, Pixel 7, release build):
  - Done in Dark:
    - keys at rest and pressed, using a held touch that was then cancelled: the face lowers, and the edge and highlight go;
    - search filter chips, with a selected chip held down;
    - calculator and unit converter wells;
    - settings panels and kicker titles.
  - Done in Paper: the search bar, chips, keys, the well and the settings panels.
  - Open:
    - a disabled key;
    - toggle-button groups (the tag editor, widget configuration);
    - an `AlertDialog`: the list dialog in Appearance is a plain `Dialog`, and the unit converter's disclaimer did not open from its icon;
    - TalkBack announcing a chip's state: `uiautomator` cannot read the launcher's search view;
    - crowding in dense rows. None was seen in the search filters.
  - The open items were checked on the device by the owner ("everything looks good").
  - `darken` stays at 0.3: only the web-search chip uses custom colours, and it looked right.
- [x] 6.3 Update `CLAUDE.md`:
  - the swap mechanism (`mise run controls`, the guard test, and what to do after an upstream merge);
  - the new seams (`LauncherCard`'s default shape, `PreferenceCategory`, the calculator and unit converter wells).

  Verify that every touched upstream file outside the import swap is listed.
