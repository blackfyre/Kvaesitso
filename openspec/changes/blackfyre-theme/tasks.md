# Tasks

## 1. Fonts

- [x] 1.1 Add fonttools to `mise.toml` (a `pipx:fonttools` tool, or the nearest working mise backend) and a `fonts` task that runs `scripts/blackfyre_fonts.py`. Verify that `mise install` succeeds and that `mise run fonts -- --help` (or an equivalent dry run) executes.
- [x] 1.2 Write `scripts/blackfyre_fonts.py`:
  - Download `Archivo[wdth,wght].ttf` and `JetBrainsMono[wght].ttf` from `google/fonts` at a pinned commit, and verify their SHA-256 sums (the sums recorded in the script).
  - Instance Archivo at `wdth=100`.
  - Write `archivo.ttf`, `jetbrains_mono.ttf` and the caps cut `bf_mono_caps.ttf` (lowercase code points remapped to their uppercase glyphs, family renamed to "BF Mono Caps") to `app/ui/src/main/res/font/`.
  - Copy both OFL texts to `app/ui/src/main/assets/licenses/`.

  Verify: running the task twice produces byte-identical files; a changed checksum makes it fail; and a check inside the script confirms that in the caps cut `cmap['a'] == cmap['A']` and `cmap['ő'] == cmap['Ő']`, and that the `wght` axis survives.

## 2. Bundled font family

- [x] 2.1 Add `FontFamily.Bundled(name)` (`@SerialName("bundled")`, variable) to `data/themes/.../typography/Typography.kt`. Map `archivo`, `jetbrains_mono` and `bf_mono_caps` in `app/ui`'s `fontFamilyOf` to the `res/font` files, with `FontVariation` weights 400, 500 and 600, falling back to `FontFamily.Default` for unknown names. Verify that `mise run build` passes, and add a JVM test that a `Bundled` family survives a JSON round trip through the theme serialiser.

## 3. Blackfyre theme definitions

- [x] 3.1 Add `BlackfyreThemeId` to `DefaultThemes.kt`, and create `themes/blackfyre/BlackfyreColors.kt` with the light and dark schemes exactly as in the design's mapping table (static colours, empty core palette). Add a JVM test in `data:themes` that computes WCAG contrast for every foreground/background pair in the table, for both schemes, and asserts that each is at least 4.5:1. Verify with `mise exec -- ./gradlew :data:themes:testDebugUnitTest` (add `testImplementation(libs.bundles.tests)` to the module if it is missing).
- [x] 3.2 Create `BlackfyreShapes.kt` (rounded corners, slots as in the design) and `BlackfyreTypography.kt` (font keys `plain`, `brand`, `mono` and `caps`; text styles as in the design's typography table; upstream's emphasised styles). Verify that `mise run build` passes.
- [ ] 3.3 Register Blackfyre in `ColorsRepository`, `ShapesRepository` and `TypographyRepository` (built-in list and `get`). Verify on the device that Blackfyre appears in each theme list under Settings → Appearance, can be selected and duplicated, and cannot be edited or deleted in place.
- [ ] 3.4 Change the defaults of `uiColorsId`, `uiShapesId` and `uiTypographyId` in `LauncherSettingsData` to the Blackfyre UUID, with a comment naming `BlackfyreThemeId`. Add a JVM test, in a module that sees both, asserting the two UUIDs are equal. Verify on the device: after clearing the fork's app data, the launcher starts in Blackfyre.

## 4. Verification on the device

- [ ] 4.1 With the device in light mode and then dark mode, check the home screen, search, a widget, a bottom sheet and a settings screen against the spec:
  - Paper and Dark backgrounds, surfaces and text colours;
  - no `bf-accent` text on light backgrounds;
  - Archivo headings at weight 600 and body text;
  - uppercase, tracked mono labels;
  - TalkBack announcing a label in its original case.

  Record screenshots in the change folder.
- [ ] 4.2 Check that display-style text at line height 1.0 is not clipped, and test the brand's 0.9–0.95. Keep the brand values if they render cleanly, and update the design's typography table to match the result.
- [x] 4.3 Run `mise run build` and `mise run test`. Add a `CLAUDE.md` note listing the upstream seams this change touches (the three repositories, the `LauncherSettingsData` defaults, `FontFamily`, `fontFamilyOf`).
