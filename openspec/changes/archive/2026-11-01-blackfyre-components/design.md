# Design

## Context

See proposal.md for the motivation and specs/blackfyre-components/spec.md for the requirements.

**Current state:**
- `blackfyre-depth` added `LocalBlackfyreDepth`, provided by `LauncherTheme` only with the Blackfyre colour theme. It also added `Modifier.blackfyrePanel(shape)` and `blackfyreElevation`, applied in `LauncherCard` and the bottom sheets.
- Material controls in `app/ui`, counted with `grep` on 2026-11-01. The swapped set is used in 53 files, and 8 files import `androidx.compose.material3.*`:

  | Control | Files |
  |---|---|
  | `Button` | 24 |
  | `TextButton` | 21 |
  | `OutlinedButton` | 14 |
  | `FilledTonalButton` | 10 |
  | `IconButton` | 37 |
  | `FilledIconButton` | 2 |
  | `FilledTonalIconButton` | 7 |
  | `FilterChip` | 7 |
  | `AssistChip` | 2 |
  | `InputChip` | 3 |
  | `ToggleButton` | 8 |
  | FABs | 4 |
  | `AlertDialog` | 14 |
- `LauncherCard` defaults to `MaterialTheme.shapes.medium` (12 dp in Blackfyre).
- `PreferenceCategory` clips its group to `shapes.medium` and sets its title in `titleSmall`/`secondary`.
- The digital clock faces, such as `DigitalClock2`, use the display styles. `ClockWidget.Clock()` already wraps the faces in the fork's `ClockFaceColors`.
- Compose Material 3 is at 1.5.0-beta02. It has expressive overloads: `Button` with `shapes`, and `ToggleButton` with `ToggleButtonShapes`.

**Source values** are from the Design System artifact (version 97: README, `tokens.json`, the `Buttons` preview) and the site's `tokens.css` and templates, which give the Dark values of the secondary key:
- **Light key:** `background: var(--bf-paper); box-shadow: 0 2px 0 var(--bf-line-strong), inset 0 1px 0 var(--bf-highlight)`, radius 10 px.
- **Primary key:** `#e2551d`, `bf-on-accent`, `0 2px 0 #a93a0f, inset 0 1px 0 rgba(255,255,255,.25)`.
- **Pressed:** `translateY(2px); box-shadow: none`.
- **Dialog:** `0 0 0 1px #33332f, 0 20px 60px rgba(0,0,0,.5)`.
- **Well:** `inset 0 2px 5px rgba(0,0,0,.55), inset 0 0 0 1px #000`.
- **`micro`:** 10 px, 0.12 em; **`kicker`:** 11 px, 0.1 em.
- **Theme colours, Paper / Dark:**

  | Token | Paper | Dark |
  |---|---|---|
  | `bf-paper` | #e6e4df | #1b1b19 |
  | `bf-line-strong` | #bfbbb3 | #0e0e0d |
  | `bf-highlight` | #fff | #33332f |
  | `bf-pressed` | #1b1b19 | #3a3935 |
  | `bf-on-pressed` | #f3f2ee | #f3f2ee |
  | `bf-ink-code` | #121211 | #0b0b0a |

## Goals / Non-Goals

**Goals:**
- One fork-owned implementation per control family, delegating to Material when Blackfyre is inactive.
- Call sites change by import only. The rewrite is a script, and a test enforces it.
- Accessibility, semantics and behaviour unchanged: roles, `selected` and `toggleable` state, click handling, and the minimum touch target.

**Non-Goals:**
- Swapping `TextButton`, `IconButton`, `Switch`, `Slider`, `Checkbox` or `RadioButton` (proposal, Departures).
- Plates and the sheen (proposal, Departures).
- Other numbers in widgets, such as calendar dates and media times, and the accent full stop on titles: no display-title surface in the launcher calls for them.
- Bottom-sheet corners: they keep Material's top corners.

## Decisions

### 1. Same-name composables in a fork package

`de.mm20.launcher2.ui.component.blackfyre` declares `Button`, `FilledTonalButton`, `OutlinedButton`, `FilledIconButton`, `FilledTonalIconButton`, `FloatingActionButton`, `SmallFloatingActionButton`, `ExtendedFloatingActionButton`, `FilterChip`, `AssistChip`, `InputChip`, `ToggleButton` and `AlertDialog`.

- **Signatures:** each has the same parameter names, types and order as each Material 3 overload that `app/ui` calls. The overloads are enumerated with `grep` in task 1.1, not assumed.
- **Delegation:** each reads `LocalBlackfyreDepth`. When it is `null`, the composable calls the Material function with every argument passed through. The fork file imports the Material functions under aliases, such as `import androidx.compose.material3.Button as M3Button`.
- **`colors`:** where Material's parameter is `colors: ButtonColors = ButtonDefaults.buttonColors()`, the fork's is `colors: ButtonColors? = null`. This is still source-compatible: callers that pass colours still compile. `null` means the key's own palette. A non-null value is honoured: its container and content colours are used, with the edge from `darken(container)`.
- **`shape`, `elevation`, `border`:** accepted and ignored under Blackfyre, since the key defines them. Passed through otherwise.
- **Alternative: restyle Material controls by theme only.** Material 3 offers no global hook for a control's shape or shadow per type. Defaults come from `ButtonDefaults` read at each call.
- **Alternative: different names (`BfButton`).** Every call would be renamed, not only its import, multiplying merge conflicts.

### 2. The key: one internal composable

`Key(onClick, modifier, enabled, palette, cornerRadius, minHeight, contentPadding, interactionSource, pressedLook, semantics, content)` draws every keycap:

- **Face:** a `Surface`-free `Box`, with `Modifier.semantics` set to the Material control's role, `clickable` or `toggleable`/`selectable` with the caller's `interactionSource`, and `minimumInteractiveComponentSize()`.
- **Edge:** drawn behind the face (`drawBehind`) as the face's shape offset down by the edge height, in the edge colour. The highlight is a 1 dp top inner band, the same technique as `blackfyrePanel`'s.
- **Pressed and held:** `collectIsPressedAsState()` drives a `graphicsLayer { translationY }` of the edge height. Through `animateFloatAsState` this takes about 50 ms; the change is immediate when `ValueAnimator.areAnimatorsEnabled()` is false. A key held down (`pressedLook`: a selected chip or checked toggle) uses the same lowered face, with no edge, and the `bf-pressed` palette.
- **Content:** `ProvideContentColorTextStyle(palette.content, MaterialTheme.typography.labelLarge)` (or `micro` for chips), then a `Row` with Material's content padding (`ButtonDefaults.ContentPadding`, or the caller's).

The palettes are `KeyPalette(face, content, edge, highlight)` values for Primary, Secondary(Paper/Dark), Pressed(Paper/Dark), and Custom(container, content). They are unit-tested against the token values.

### 3. Chips and toggles

- `FilterChip(selected, …)` and `ToggleButton(checked, …)` use `toggleable`/`selectable` semantics as Material does, and show a held-down key when `selected` or `checked`.
- Chip icons (`leadingIcon`, `trailingIcon`, `avatar`) are laid out as in Material: 18 dp icons, 8 dp gaps.
- The label style is a new fork `micro` `TextStyle`: BF Mono Caps, 10 sp, letter spacing 0.12 em, line height 1.3. It is derived from `MaterialTheme.typography.labelSmall`'s family, so it follows the bundled font.

### 4. Dialogs

The fork's `AlertDialog`:
- under Blackfyre, calls Material's `AlertDialog` with `shape = RoundedCornerShape(22.dp)`, `containerColor = colorScheme.surface`, `tonalElevation = 0.dp`, and a `modifier` that adds the dialog shadow: the 1 dp ring and a 20 dp-down, 60 dp-blur black 50% shadow, drawn with the depth file's shadow painter;
- otherwise passes everything through.

Text buttons inside dialogs are unchanged.

### 5. Seams for surfaces

**`LauncherCard`:** the default `shape` becomes `blackfyreShape(MaterialTheme.shapes.medium)`. This returns `RoundedCornerShape(22.dp)` while depth is active and the given shape otherwise. Callers that pass a shape keep it.

**`PreferenceCategory`:**
- the title takes `blackfyreKicker(MaterialTheme.typography.titleSmall)`, which returns the kicker style while active, in `onSurfaceVariant`; otherwise the given style and `secondary`;
- the group `Column` takes `.blackfyrePanel(shape)`, and its clip shape comes from `blackfyreShape(shapes.medium)`.

**Wells:** a fork `Modifier.blackfyreWell(shape)`:
- a `bf-ink-code` background;
- an inset shadow (`innerShadow`, or the band technique with `BlurMaskFilter`) of 0/2 dp, blur 5 dp, black 55%;
- a 1 dp inner black ring.

Its content is provided with `bf-on-panel` and a mono style (`MaterialTheme.typography` with the `mono` family, `fontFeatureSettings = "tnum"`). It is applied in `CalculatorItem` and in the unit converter's value rows, as one wrapper each. Wells do not read the light.

**Clock:** `ClockFaceColors` (already wrapping the faces) also provides a `MaterialTheme` typography whose display styles use JetBrains Mono with `tnum`, while depth is active. This needs no edits in the faces.

### 6. The swap script and the guard

**`scripts/blackfyre_controls.py`** (Python 3, no dependencies; `mise run controls`):
- for each `.kt` under `app/ui/src/main/java` outside the fork package, it replaces `import androidx.compose.material3.<Name>` with `import de.mm20.launcher2.ui.component.blackfyre.<Name>`, for the swapped names;
- for files with `import androidx.compose.material3.*` that call a swapped name, it adds the fork import, which takes precedence over a star import;
- it is idempotent, and prints the files it changed.

**`BlackfyreControlImportsTest`** (JVM, `app/ui/src/test`) scans the same sources and fails, naming each file, when:
- a swapped Material import remains; or
- a star-importing file calls a swapped name without the fork import.

Name matching uses `\bName\s*\(` (and `{` for trailing lambdas), so `ButtonDefaults` and `IconButton` do not match `Button`.

### 7. File placement

**New fork-owned files:**
- `app/ui/.../ui/component/blackfyre/`: `Buttons.kt`, `Chips.kt`, `Toggles.kt`, `Dialogs.kt`, `Key.kt`, `KeyPalettes.kt`, `Wells.kt`;
- in `theme/blackfyre/BlackfyreDepth.kt`: the `blackfyreShape` and `blackfyreKicker` helpers, next to `blackfyreElevation`;
- the tests;
- the script.

**Upstream files touched:**
- the import swaps;
- `LauncherCard.kt`, `PreferenceCategory.kt`, `CalculatorItem.kt`, `UnitConverterResults.kt`;
- `ClockFaceColors.kt` (already fork-owned).

## Risks / Trade-offs

- [Signature drift: an upstream Compose bump changes a Material control's parameters, or upstream starts calling another overload] → The build fails at the call site, so it cannot pass silently. The fork composable gains the overload. The guard test does not cover parameters; the compiler does.
- [53 upstream files each get one changed import line] → Chosen by the owner (scope question, 2026-11-01). A conflict is one line, and the script plus the guard re-establish the swap after any merge.
- [Caller-specific styling is lost under Blackfyre: custom shapes, elevations and borders] → Accepted: the key defines them. Colours are honoured, so semantic colours (for example an error-coloured button) survive.
- [Keys are taller than Material's 40 dp buttons (44 dp minimum)] → This matches the brand's touch-target rule. Dense rows such as the search filters are checked on the device (task 6.2).
- [Expressive `ToggleButton` and `ButtonGroup` layouts assume Material's shape morphing] → Under Blackfyre the fork's toggle ignores `shapes`. Grouped toggles become separate keys with their group's spacing. Checked on the device.
- [Dark well ground in Paper next to light cards is a strong contrast] → This is the brand's stat-tile look. Device check.

## Migration Plan

There is no data. Selecting another colour theme restores Material rendering. A rollback reverts the commits; the swap is reversed by inverting the script's mapping, which is the same script run with `--revert`.

## Open Questions

- The exact `darken` factor for custom-colour key edges, starting at 30% towards black. It is tuned on the device and changes nothing else.
