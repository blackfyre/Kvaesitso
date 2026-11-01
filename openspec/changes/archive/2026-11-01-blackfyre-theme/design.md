# Design

## Context

See `proposal.md` for motivation and `specs/blackfyre-theme/spec.md` for the required behaviour.

**Token source.** The Blackfyre Design System artifact's `project/tokens.json` (version 1, synced 2026-10-28), mirrored for colours by `legendary-winner`'s `assets/css/tokens.css`. Values in this design are copied from it. A token with no Dark value keeps its Paper value in Dark, as `tokens.css` does.

**Kvaesitso's theme model** (`data:themes`; applied by `app/ui/.../theme/LauncherTheme.kt` through `MaterialExpressiveTheme`):

- **Colours:** `Colors` holds a light and a dark `ColorScheme` of Material 3 roles. Each role is a `StaticColor` (ARGB) or a reference into a core palette.
- **Shapes:** `Shapes` holds a base shape and optional per-slot overrides (`extraSmall` … `extraExtraLarge`), each with corner style and radii in dp.
- **Typography:**
  - `Typography` holds a keyed map of font families (upstream uses the keys `brand` and `plain`) and per-slot `TextStyle`s. Each style refers to a font key and sets size (sp), weight, line height (em) and letter-spacing (em).
  - `FontFamily` is a sealed, serialisable type: `LauncherDefault` (bundled Google Sans Flex), `DeviceHeadline`, `DeviceBody`, `SansSerif`, `Serif`, `Monospace` and `System(name)`. `app/ui` maps it to Compose fonts in `fontFamilyOf`.
- **Built-ins:** themes are hard-coded per repository with fixed UUIDs (`DefaultThemes.kt`). The user's choice is stored as a UUID per kind in `LauncherSettingsData` (`uiColorsId`, `uiShapesId`, `uiTypographyId`, `uiTransparenciesId`). These default to `UUID(0L, 0L)`, the upstream default, and are written with `encodeDefaults = true`, so an existing install has them stored explicitly.
- **Transparencies:** `background` 0.85, `surface` 1, `elevatedSurface` 1 by default, which already suits opaque Blackfyre panels.

**Constraints:** keep the upstream diff small and localised; never invent token values.

## Goals / Non-Goals

**Goals:**

- Blackfyre colours, type and shapes everywhere the Material theme reaches, as the default, through the existing model.
- Uppercase, tracked mono labels with no call-site edits.

**Non-Goals:**

- **Depth, keycap controls, mono numbers in specific widgets, panel radius on cards, `micro`-size labels:** these are component-layer work for `blackfyre-depth` and `blackfyre-components` (see Follow-up changes).
- **A Blackfyre transparency theme:** the upstream default already gives opaque surfaces.
- **The E-ink themes and the hidden themes** (`normandy`, `samurai`).
- **Bundled fonts in the font picker:** the typography editor's picker will not list the bundled fonts. A duplicated Blackfyre typography keeps them.
- **Theme files:** a `.kvtheme` exported with the Blackfyre typography will not import into stock Kvaesitso, which does not know the bundled font type. That is acceptable for a personal fork.

## Decisions

### 1. Built-in themes, made the default through the settings defaults

The design adds `BlackfyreThemeId = UUID(0x00B1ACF1E0L, 0L)` to `DefaultThemes.kt`. It adds a Blackfyre entry to the built-in lists and `get()` of `ColorsRepository`, `ShapesRepository` and `TypographyRepository`, about three lines each. The Blackfyre definitions live in new files: `themes/blackfyre/BlackfyreColors.kt`, `BlackfyreShapes.kt` and `BlackfyreTypography.kt`. In `core:preferences`' `LauncherSettingsData`, the defaults of `uiColorsId`, `uiShapesId` and `uiTypographyId` become the same UUID. `core:preferences` cannot depend on `data:themes`, so the UUID literal is repeated there with a comment, and a unit test checks that the two are equal. `uiTransparenciesId` keeps the upstream default.

- **Alternative: seed the themes as user rows at first start.** The themes would become editable and deletable, and seeding has to be idempotent.
- **Alternative: replace the upstream default theme's values.** The upstream default could then no longer be selected, and every upstream change to the defaults would conflict.
- **Alternative: a settings migration that switches stored upstream-default IDs to Blackfyre.** It cannot tell "never chosen" from "chose the upstream default", and fork migrations would collide with upstream's numbering.
- **Consequence:** fresh installs start in Blackfyre. Existing installs, including the fork's current test install, keep their stored choice until the user selects Blackfyre once in the appearance settings, or clears the fork's app data.

### 2. Colour role mapping

The mapping is lossy, because Blackfyre's semantic tokens collapse into Material 3 roles. The governing choice: `primary` carries `bf-accent-text`, not `bf-accent`. Material 3 draws `primary` as text and icons (text buttons, links, selected tabs, switches), and Blackfyre forbids the accent as text on light. The brand orange `bf-accent` goes to `primaryContainer`, with `bf-on-accent` on it. The component layer later uses `bf-accent` directly for primary actions. Contrast was computed for every foreground/background pair below; all reach at least 4.5:1, and a unit test pins this.

| Role | Paper (light) | Dark |
|---|---|---|
| primary / onPrimary | `bf-accent-text` #a3390c / `bf-white` #ffffff | `bf-accent-text` #ff8a4c / `bf-on-accent` #1b1b19 |
| primaryContainer / onPrimaryContainer | `bf-accent` #e2551d / `bf-on-accent` #1b1b19 | `bf-accent` #e2551d / `bf-on-accent` #1b1b19 |
| inversePrimary | `bf-accent-on-dark` #ff8a4c | `bf-accent-text` (Paper) #a3390c |
| secondary / onSecondary | `bf-ink` #1b1b19 / `bf-on-panel` #f3f2ee | `bf-ink` #f3f2ee / `bf-on-accent` #1b1b19 |
| secondaryContainer / onSecondaryContainer | `bf-surface-sunk` #dedbd4 / `bf-ink` #1b1b19 | `bf-surface-sunk` #2c2c29 / `bf-ink` #f3f2ee |
| tertiary / onTertiary | `bf-ink-soft` #4a4944 / `bf-surface` #f3f2ee | `bf-ink-soft` #c9c6bf / `bf-on-accent` #1b1b19 |
| tertiaryContainer / onTertiaryContainer | `bf-paper-deep` #d9d6cf / `bf-ink` #1b1b19 | `bf-paper-deep` #161615 / `bf-ink` #f3f2ee |
| error / onError | `bf-alert-text` #c2301f / `bf-white` #ffffff | `bf-alert-text` #ff6a4c / `bf-on-accent` #1b1b19 |
| errorContainer / onErrorContainer | `bf-surface-hover` #f8f7f4 / `bf-alert-text` #c2301f | `bf-surface-sunk` #2c2c29 / `bf-alert-text` #ff6a4c |
| background / onBackground | `bf-paper` #e6e4df / `bf-ink` #1b1b19 | `bf-paper` #1b1b19 / `bf-ink` #f3f2ee |
| surface / onSurface | `bf-surface` #f3f2ee / `bf-ink` #1b1b19 | `bf-surface` #242421 / `bf-ink` #f3f2ee |
| surfaceVariant / onSurfaceVariant | `bf-surface-sunk` #dedbd4 / `bf-ink-soft` #4a4944 | `bf-surface-sunk` #2c2c29 / `bf-ink-soft` #c9c6bf |
| surfaceContainerLowest | `bf-highlight` #ffffff | `bf-paper-deep` #161615 |
| surfaceContainerLow | `bf-surface-hover` #f8f7f4 | `bf-paper` #1b1b19 |
| surfaceContainer | `bf-surface` #f3f2ee | `bf-surface` #242421 |
| surfaceContainerHigh | `bf-paper` #e6e4df | `bf-surface-hover` #2c2c29 |
| surfaceContainerHighest | `bf-surface-sunk` #dedbd4 | `bf-card-edge` #3a3935 |
| surfaceDim / surfaceBright | `bf-paper-deep` #d9d6cf / `bf-surface-hover` #f8f7f4 | `bf-paper-deep` #161615 / `bf-surface-hover` #2c2c29 |
| surfaceTint | `bf-surface` #f3f2ee | `bf-surface` #242421 |
| outline / outlineVariant | `bf-ink-hint` #68665f / `bf-line` #d3d0c9 | `bf-ink-hint` #8f8c84 / `bf-line` #33332f |
| inverseSurface / inverseOnSurface | `bf-panel` #1b1b19 / `bf-on-panel` #f3f2ee | `bf-ink` #f3f2ee / `bf-paper` #1b1b19 |
| scrim | #000000 (`bf-panel-edge`) | #000000 (`bf-panel-edge`) |

- `surfaceTint` equals `surface`, so Material's tonal elevation adds no colour cast; Blackfyre expresses elevation with depth instead.
- `outline` uses `bf-ink-hint`, not a line token. Material draws the thumb and border of a switch that is off, and the borders of outlined controls, in `outline` on `surfaceContainerHighest` and the other surfaces. The line tokens are too faint for that: `bf-line-strong` reaches only 1.38:1 on the Paper track, and Dark `bf-line-strong` (#0e0e0d) is darker than the page. `bf-card-edge`, the first Dark choice, equalled the Dark track (`surfaceContainerHighest`) and hid the switch entirely (found on the device after task 4.1). `bf-ink-hint` reaches at least 3:1 on every surface role in both schemes, the WCAG level for non-text boundaries, and a unit test pins this.
- `onPrimaryContainer` stays `bf-on-accent`, as the accent rule requires. Upstream's watch faces, however, draw light content on a dark wallpaper in `onPrimaryContainer` when the launcher is dark, assuming Material's tonal scheme, where that role is a light tint; in Dark Blackfyre it equals the page colour and the clock vanished. A fork-owned `ClockFaceColors` wraps the watch faces in upstream's `Clock()` and, when the launcher is dark and `onPrimaryContainer` is darker than `primaryContainer`, gives them `primary` (`bf-accent-on-dark`) in that role. Tonal schemes are unaffected.
- The light `errorContainer` uses `bf-surface-hover`, because `bf-alert-text` on `bf-surface-sunk` is only 4.07:1.
- The core palette stays empty: every role is a static colour.

### 3. Typography mapping

The font keys are `plain` and `brand` → Archivo, `mono` → JetBrains Mono and `caps` → BF Mono Caps (decision 4). Material 3's slot sizes are kept, because they are tuned for a phone and Blackfyre's sizes are desktop web maxima. Each slot takes the family, weight, line height and tracking of the Blackfyre style in the same role:

| Material 3 slots | Blackfyre style | Font, weight, line height (em), letter-spacing (em) |
|---|---|---|
| displayLarge / Medium / Small | display / title / headline | Archivo 600; 0.9 / 0.95 / 1.05; −0.045 / −0.04 / −0.03 |
| headlineLarge / Medium / Small | h2 / h3 / h4 | Archivo 600; 1.15 / 1.1 / 1.2; −0.02 / −0.02 / −0.01 |
| titleLarge / Medium / Small | h4, statement weight | Archivo 600 / 600 / 500; 1.2; −0.01 / 0 / 0 |
| bodyLarge / Medium / Small | body / body-sm | Archivo 400; 1.5; 0 |
| labelLarge | label (button size 13) | BF Mono Caps 400, 13 sp; 1.3; 0.06 |
| labelMedium | label | BF Mono Caps 400, 12 sp; 1.3; 0.06 |
| labelSmall | kicker | BF Mono Caps 400, 11 sp; 1.3; 0.1 |

- Display line heights use Blackfyre's values (display 0.9, title 0.95). Tight line heights risked clipped ascenders and descenders in Compose text layout, so they were checked on the device (Pixel 7, task 4.2): four lines of Archivo 600 at 57 sp and 0.9 render without clipping or overlapping glyphs. Upstream's `DigitalClock1` overrides `displayLarge` to 0.8em, which also renders cleanly.
- Body line height is 1.5 rather than the site's 1.6–1.7: launcher lists are dense, and the site's value is for long-form reading. This is a deliberate deviation, recorded here.
- The emphasised styles keep upstream's relative weights.

### 4. Uppercase through a derived caps font

`scripts/blackfyre_fonts.py` copies JetBrains Mono and remaps its `cmap`, so that every lowercase code point that has an uppercase mapping (`str.upper()` giving one character that the font contains: a–z, á, é, ő, ű and the rest of Latin-1 and Latin Extended-A) points to the uppercase glyph. It renames the family to "BF Mono Caps" in the `name` table, keeps the variable `wght` axis, and leaves the outlines untouched. The text is unchanged, so accessibility, copy and search see the original case.

- **Alternative: call `.uppercase()` at call sites.** That means hundreds of edits across upstream files, and it changes what screen readers announce. The user chose the caps font.
- **Alternative: an OpenType case feature.** JetBrains Mono has no small-caps or all-caps feature that maps lowercase to capitals.
- **Licence:** JetBrains Mono's OFL declares no Reserved Font Name. The derived font is renamed anyway and ships with the OFL text.

### 5. Bundled fonts as a new `FontFamily` kind

The design adds `@SerialName("bundled") data class Bundled(val name: String) : FontFamily, VariableFontFamily` to the sealed interface, and a branch in `app/ui`'s `fontFamilyOf` that maps `archivo`, `jetbrains_mono` and `bf_mono_caps` to `res/font` resources. They are variable fonts, so their weights are set with `FontVariation.Settings(FontVariation.weight(n))` for 400, 500 and 600. Unknown names fall back to `FontFamily.Default`.

- **Alternative: reuse `System(name)`.** It resolves only fonts installed on the device.
- **Font files:** variable TTFs from `google/fonts` at a pinned commit (`ofl/archivo/Archivo[wdth,wght].ttf`, instanced to `wdth=100` to drop the unused axis, and `ofl/jetbrainsmono/JetBrainsMono[wght].ttf`), verified by SHA-256. The site's WOFF2 files are split into `latin` and `latin-ext` subsets, and fonttools cannot merge variable fonts, so they are not used.
- **Tooling:** `mise` pins `uv`, and the `mise run fonts` task runs the script with `uv run --script`; the script's inline metadata pins `fonttools==4.68.0`. A `pipx:fonttools` tool would provide only the fonttools command-line programs, not the importable library that the cmap remap needs. The generated TTFs and the OFL texts are committed (`app/ui/src/main/res/font/`, `app/ui/src/main/assets/licenses/`), so normal builds need no network.

### 6. Shape mapping

The base shape is rounded with 12 dp corners. The slots are: extraSmall 4 (`bf-radius-xs`), small 8 (`bf-radius-md`), medium 12 (`bf-radius-xl`), large 14 (`bf-radius-2xl`), and largeIncreased, extraLarge, extraLargeIncreased and extraExtraLarge all 22 (`bf-radius-panel`). Panels using the 22 dp radius on the home screen's cards is component-layer work: `LauncherCard` uses `medium` today.

### Follow-up changes (outline, not part of this change)

- **`blackfyre-depth`:**
  - Panel, plate and well depth as Compose modifiers, using `dropShadow` and `innerShadow` with offsets mirroring `bf-depth-*`.
  - A fork-owned light-source flow from the gravity sensor, sampled while the launcher is resumed, smoothed over 250 ms, and overhead under reduced motion.
  - Applied to `LauncherCard`, widgets, search bar, sheets and popups.
- **`blackfyre-components`:**
  - Keycap and pill controls as fork-owned composables, signature-compatible with the Material 3 ones they replace (`Button`, `TextButton`, `OutlinedButton`, `FilledTonalButton`, `IconButton`, `FilterChip`, `ToggleButton` …).
  - Each call site switches by changing only its import, which keeps merges to one-line conflicts.
  - Mono numbers in widgets, `micro` labels, the 22 dp panel radius, the accent full stop on display titles where applicable, across the launcher and settings.

## Risks / Trade-offs

- [Lossy role mapping: some Material 3 components use roles in ways the brand would not, such as filled buttons in `primary`, which is rust rather than brand orange] → Accepted for the theme layer. `blackfyre-components` replaces those controls.
- [The caps font changes every label slot, including places where upstream wanted mixed case, such as the clock's date line if it uses a label style] → Matches the brand rule. A specific exception can be handled in the component layer by using a body style there.
- [Archivo's `wdth` axis is instanced away] → Blackfyre uses only normal width.
- [The added font files grow the APK by roughly 300–600 KB] → Acceptable for a personal build.
- [Upstream edits in three repositories and in `LauncherSettingsData`'s defaults] → Each is a few lines at a stable seam. Noted in `CLAUDE.md`.
- [Upstream adds a new Material 3 role or text slot] → It falls back to Material's default value until it is mapped. The contrast test lists the roles it checks.

## Migration Plan

No data migration. Fresh installs use Blackfyre, and existing installs keep their stored theme until the user picks Blackfyre. Rollback: revert the change; a stored Blackfyre ID then resolves to the upstream default through `getOrDefault`'s existing fallback for unknown IDs.
