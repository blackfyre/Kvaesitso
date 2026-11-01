# Tasks

## 1. Light source

- [x] 1.1 Add `lightFromGravity(gx, gy, gz, rotation)` and the 250 ms low-pass smoothing as pure functions in `BlackfyreLight.kt`. Verify with JVM tests:
  - flat face up gives (0, 0);
  - upright in portrait gives cy = 1;
  - right edge down gives cx > 0;
  - each display rotation maps to screen axes;
  - values are clamped to −1…1;
  - the smoothing reaches about 63% of a step after 250 ms and is exact on the first sample.
- [x] 1.2 Add the `BlackfyreLight` flow: `TYPE_GRAVITY` at `SENSOR_DELAY_UI` in a `callbackFlow`, `stateIn` with `WhileSubscribed`, and the constant `Light.Overhead` when animators are disabled or no gravity sensor exists. Verify by building (`mise run build`); device checks follow in 4.2.

## 2. Depth modifier

- [x] 2.1 Add `BlackfyreDepth.kt`:
  - `LocalBlackfyreDepth` and `ProvideBlackfyreDepth`;
  - the Paper and Dark panel colour sets, from the values in design.md "Source values";
  - `panelCastOffset`;
  - `blackfyreElevation`.

  Verify with JVM tests: the offsets are (0, 10) dp overhead and (±18, 10 ± 9) dp at the extremes, and the colour sets match the token values.
- [x] 2.2 Implement `Modifier.blackfyrePanel(shape)`: shadows in a difference clip, the highlight as a 1 dp inner shadow, and the light read at draw time through `collectAsStateWithLifecycle(minActiveState = RESUMED)`. Verify on the device with a temporary preview or screenshot, in Paper and Dark, on an opaque and a translucent card:
  - no shadow is visible inside the translucent card;
  - no seam shows at the edge.

  Record which shadow drawing path was kept.

  Outcome (2026-11-01, Pixel 7): the Android `Paint` with `BlurMaskFilter` path is used, drawn in a `clipPath(…, ClipOp.Difference)`. Both Paper and Dark showed no shadow inside translucent cards and no seam at the edges (search bar, result cards, item popup, favourites sheet).

## 3. Seams

- [x] 3.1 `LauncherTheme`: wrap the content in `ProvideBlackfyreDepth(enabled = themeColors.id == BlackfyreThemeId, dark = darkTheme)`. Verify by building.
- [x] 3.2 `LauncherCard`: apply `blackfyrePanel(shape)` when `backgroundOpacity > 0`, and route `shadowElevation` and `tonalElevation` through `blackfyreElevation`. Verify on the device:
  - search results, widgets, the search bar, a home-screen card and an item popup show depth;
  - a widget without a background and the transparent search bar show none.
- [x] 3.3 `BottomSheet` and `DismissableBottomSheet`: apply `blackfyrePanel(BottomSheetDefaults.ExpandedShape)` and route `shadowElevation` through `blackfyreElevation`. Verify on the device by opening the widget picker sheet and a tag editor sheet.

## 4. Behaviour checks and documentation

- [x] 4.1 Run `mise run test` and `mise run build`; all tests pass.
- [x] 4.2 Device checks on the Pixel 7 (release build):
  - **Tilt:** shadows follow the tilt with a short lag and without jitter while still.
  - **Pause:** with another app in front, `adb shell dumpsys sensorservice` lists no gravity listener for the launcher; on returning home, it does again.
  - **Remove animations:** with `animator_duration_scale` set to 0, shadows are overhead and no listener is registered. Restore the previous scale afterwards.
  - **Other theme:** with a non-Blackfyre colour theme, the cards match upstream and no listener is registered.
  - **Upright posture:** judge whether the shadows' resting position reads well (design risk 1). Record the outcome.

  Progress (2026-11-01):
  - Done: pause and resume (registered at home and in search, unregistered with Settings in front, registered again on return); Remove animations (no registration at scale 0; the previous scale of 0.5 was restored); other theme (Default colours: flat cards, no registration; Blackfyre restored).
  - Tilt and upright posture: checked by the owner on the phone ("seems to work"); the absolute mapping is kept.
- [x] 4.3 Update `CLAUDE.md` (Theming seams): `LauncherTheme`'s `ProvideBlackfyreDepth`, `LauncherCard`, `BottomSheet` and `DismissableBottomSheet`. Verify that the list names each touched upstream file.
