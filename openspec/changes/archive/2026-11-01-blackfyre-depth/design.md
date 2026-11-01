# Design

## Context

See proposal.md for the motivation and specs/blackfyre-depth/spec.md for the requirements.

**Current state:**
- `LauncherTheme` (`app/ui/.../theme/LauncherTheme.kt`) resolves the colour theme (a `Colors` with an `id`) and builds `MaterialExpressiveTheme`. The built-in Blackfyre colours have the id `BlackfyreThemeId` (`data/themes/.../DefaultThemes.kt`).
- `LauncherCard` is a `Surface`, used by:
  - search results (`SearchColumn`);
  - item popups (`GridItem`);
  - widgets (`WidgetItem`);
  - the search bar (`SearchBar`).

  It sets `shadowElevation` only when the background is opaque (`backgroundOpacity == 1f`), because a Material shadow shows through a translucent card. The owner uses the Semi-transparent scheme, so their cards have no shadow at all today.
- `BottomSheet` and `DismissableBottomSheet` each draw their sheet as a `Surface` with `shadowElevation = 1.dp`.
- Blackfyre maps `surfaceTint` to `surface`, so tonal elevation adds no colour. Depth is meant to replace it (`blackfyre-theme` design, decision on `surfaceTint`).
- Compose is pinned at 1.13 beta (`gradle/libs.versions.toml`). It has `Modifier.dropShadow` and `Modifier.innerShadow`, with block forms that read state at draw time.

**Source values.** They are taken from the Design System artifact (`tokens.json`, version 97) and the site's `assets/css/tokens.css` (`.dp` rules), not from memory:
- `bf-depth-panel`, Paper: `inset 0 1px 0 #fff, 0 1px 2px rgba(40,36,30,.08), 0 10px 30px rgba(40,36,30,.08)`.
- `bf-depth-panel`, Dark (from the token's usage note and the site's Dark block): `inset 0 1px 0 #33332f, 0 1px 2px rgba(0,0,0,.4), 0 10px 30px rgba(0,0,0,.35)`.
- Cast offset (site, `.dp`): `calc(cx * 18px) calc(10px + cy * 9px)`, with `cx` and `cy` in −1…1 and 0 meaning the light is straight above.
- `bf-highlight`: `#ffffff` in Paper, `#33332f` in Dark.

## Goals / Non-Goals

**Goals:**
- One depth modifier, applied at a handful of narrow seams.
- A light source that costs nothing when nothing is drawn with depth.
- The depth geometry and the light maths as pure functions, unit-tested on the JVM.

**Non-Goals:**
- Plate and well depth, and the dark-plate sheen. They move to `blackfyre-components`, which introduces the keycaps and tiles that use them. This departs from the outline in the archived `blackfyre-theme` design, which listed them here: building them now would leave code that nothing uses.
- Settings screens, dialogs, and the 22 dp panel radius on cards: these are `blackfyre-components` work.
- Pointer-driven light (the site's mouse and pen behaviour): a phone has neither.

## Decisions

### 1. The depth carrier: a composition local provided by `LauncherTheme`

A fork-owned `LocalBlackfyreDepth` holds a `BlackfyreDepth?`: the Paper or Dark colours, plus access to the light. It is `null` unless the colour theme's id is `BlackfyreThemeId`.

`LauncherTheme` gets one seam: its content is wrapped in a fork composable, `ProvideBlackfyreDepth(enabled = themeColors.id == BlackfyreThemeId, dark = darkTheme)`. Every consumer reads the local, so "only with Blackfyre" is decided in one place.

- **Alternative: check the theme id in each modifier call.** That spreads the theme lookup over every seam.
- **Alternative: gate on all three Blackfyre parts (colours, shapes, type).** Depth's values are colours tuned to Blackfyre's surfaces, so the colour theme alone is the right condition.

### 2. The panel modifier: shadows clipped to outside the shape

`Modifier.blackfyrePanel(shape)` reads `LocalBlackfyreDepth`. It returns `Modifier` unchanged when the local is `null`. Otherwise it draws three layers:

1. **Cast and contact shadows.** These are drawn behind the content inside a clip that excludes the panel's outline (`clipPath(outline, ClipOp.Difference)`), so that nothing shows through a translucent background. They use Compose's shadow painter, `DropShadowPainter`, through the `Shadow`/`dropShadow` APIs; if that cannot be drawn under a custom clip, they use an Android `Paint` with `BlurMaskFilter`. Which of the two is used is decided in task 2.2, by the screenshot check.
2. **Top-edge highlight.** `Modifier.innerShadow(shape, Shadow(radius = 0.dp, offset = DpOffset(0.dp, 1.dp), color = highlight))`: CSS `inset 0 1px 0`.
3. **Light.** The cast offset is read inside the draw block from a `State`, so a change of tilt redraws the panels without recomposing them.

The geometry is a pure function, `panelCastOffset(cx, cy): DpOffset = DpOffset(cx * 18.dp, 10.dp + cy * 9.dp)`, unit-tested together with the Paper and Dark colour sets.

- **Alternative: plain `Modifier.dropShadow`.** It draws the whole blurred shape behind the content. Through a Semi-transparent card, that darkens the card's interior, which the owner's setup would show everywhere.
- **Alternative: Material `shadowElevation`.** It gives no control over offset, colour or light direction, and upstream already disables it for translucent cards.

### 3. Seams in `LauncherCard` and the bottom sheets

**`LauncherCard`** gains two changes:
- `.blackfyrePanel(shape)` on the `Surface` modifier, applied when `backgroundOpacity > 0`. A widget without a background, and the transparent or hidden search bar (which hides the card through `alpha` and a zero background opacity), get none.
- `shadowElevation` and `tonalElevation` become 0 while depth is active.

This one file covers the cards, search results, widgets, the search bar and the popups.

**`BottomSheet` and `DismissableBottomSheet`**: `.blackfyrePanel(BottomSheetDefaults.ExpandedShape)` and `shadowElevation = if (depth active) 0.dp else 1.dp`. A small fork helper, `blackfyreElevation(default)`, returns 0 when depth is active, so each seam stays one expression.

The popup's existing `elevation = 8.dp * p` animation drives Material's shadow only. Under Blackfyre the popup's depth is constant, and it appears with the popup's own transition.

### 4. The light source: gravity, screen-relative, lazily shared

`BlackfyreLight` is a fork-owned class (`app/ui/.../theme/blackfyre/`), created once per `ProvideBlackfyreDepth`. It exposes `light: StateFlow<Light>`, where `Light(cx, cy)`.

- **Sensor:** `Sensor.TYPE_GRAVITY` at `SENSOR_DELAY_UI`, in a `callbackFlow` that unregisters on close (the same pattern as `DevicePoseProvider`). `core:devicepose` is not touched, to keep the diff in fork-owned files.
- **Mapping** (pure, unit-tested): `lightFromGravity(gx, gy, gz, rotation)`.
  - Android reports gravity as the reaction vector: +x reads positive when the device's right edge points up.
  - The vector is first rotated from device axes to screen axes, using the display rotation (`Display.rotation`).
  - In screen axes, with y pointing down, the reported vector is `(sx, sy)`. Shadows fall the opposite way to it: `cx = clamp(−sx / g, −1, 1)` and `cy = clamp(−sy / g, −1, 1)`.
  - Flat, face up: both are 0. Upright in portrait, the vector points up the screen (`sy = −g`), so `cy = 1` and shadows fall down the screen. Right edge down: `sx < 0`, so `cx > 0` and shadows move right.
- **Smoothing** (pure, unit-tested): a first-order low-pass filter with a 250 ms time constant, applied per sample using the sensor timestamps (`α = 1 − exp(−Δt / 250 ms)`). This matches the brand's "lag 250 ms" and absorbs sensor noise, so a device held still does not jitter. The first sample is taken as it is.
- **Laziness:** the flow is `stateIn(scope, SharingStarted.WhileSubscribed(), Light.Overhead)`. Only panels subscribe, through `collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)`. So:
  - the sensor runs only while at least one depth panel is on a resumed screen;
  - an activity without panels (settings) never starts it;
  - pausing the launcher (an app in front, the screen off) unregisters it.
- **Reduced motion:** when `ValueAnimator.areAnimatorsEnabled()` is false (Remove animations, or an animator duration scale of 0), or `getDefaultSensor(TYPE_GRAVITY)` is null, the flow is the constant `Light.Overhead` and never registers a listener. The flag is read when the flow starts, so a change takes effect the next time the launcher resumes.

- **Alternative: a light relative to how the device is usually held**, with a rolling neutral pose. It would keep the shadow near its rest position in normal use, but it adds state and a calibration time constant. The absolute mapping matches "a light fixed above the device", is stateless, and is testable. If the shadows feel too low while the device is held upright, the neutral pose is a contained follow-up in `lightFromGravity`.
- **Alternative: `TYPE_ACCELEROMETER`.** It includes linear acceleration, so walking would shake the shadows. `TYPE_GRAVITY` is fused and already used by upstream (`DevicePoseProvider`).

### 5. File placement

- **New fork-owned files**, in `app/ui/src/main/java/de/mm20/launcher2/ui/theme/blackfyre/`:
  - `BlackfyreDepth.kt`: the local, the provider, the colour sets, `blackfyrePanel`, `blackfyreElevation`, `panelCastOffset`;
  - `BlackfyreLight.kt`: the sensor flow, `lightFromGravity`, the smoothing.
- **Tests**, in `app/ui/src/test/.../theme/blackfyre/`. The JVM test setup is already in place: `testImplementation(libs.bundles.tests)`.
- **Upstream files touched:** `LauncherTheme.kt`, `LauncherCard.kt`, `BottomSheet.kt`, `DismissableBottomSheet.kt`. `CLAUDE.md`'s Theming seam list gains these.

## Risks / Trade-offs

- [The device is usually held near upright, so `cy` sits near 0.7–0.9 and shadows usually fall about 16–18 dp down, more than the site's rest value of 10 dp] → This follows from a light fixed above the device in the world. The device check in the tasks judges whether it reads well; if not, the neutral-pose alternative (decision 4) is contained in one pure function.
- [The difference clip with a blurred shadow may draw a visible seam at the card's edge, or `DropShadowPainter` may not respect the clip] → Task 2.2 checks this with screenshots in both modes before the seams are wired; the `BlurMaskFilter` path is the fallback.
- [Battery: the gravity sensor runs while the home screen is visible] → `TYPE_GRAVITY` at the UI rate is a low-power fused sensor. It runs only while resumed and while panels are on screen, and never under reduced motion.
- [Redraw cost: every panel redraws on each light change (about 16 per second)] → Reading the offset at draw time avoids recomposition and layout. Shadows are cheap at this count (fewer than about 15 panels on screen).
- [Upstream changes `LauncherCard` or the sheets] → These are short seams listed in `CLAUDE.md`. A conflict there is a few lines.
- [Material's elevation is turned off under Blackfyre, including on the dragged widget's 8 dp lift] → The dragged widget keeps its depth and its z-order; the lift is a component-layer affordance and can be added in `blackfyre-components`.

## Migration Plan

There is no stored data. Selecting another colour theme removes all depth. A rollback is reverting the change's commits.
