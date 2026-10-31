# Proposal

## Why

The fork is restyled with the Blackfyre design system, at both the theme layer and the component layer. This change is the theme layer, which everything else builds on. It makes Blackfyre's colours, type and shapes the launcher's default theme through Kvaesitso's existing theme model, so that every screen picks up the brand before any Composable is restyled.

## What Changes

- A built-in Blackfyre theme for colours, shapes and typography, mapped from the design system's tokens. It is the default for new installs. Kvaesitso's own built-in themes stay selectable.
- **Colours:** Blackfyre Paper becomes the light colour scheme and Blackfyre Dark the dark one, following the system's light/dark setting as Kvaesitso already does. The mapping keeps Blackfyre's contrast rules: the accent is never used as text on a light background, and text on the accent uses `bf-on-accent`.
- **Type:**
  - Archivo is bundled for everything that is read, with weight 600 for headings.
  - JetBrains Mono is bundled for the label styles, tracked out, in a derived capitals-only cut, so labels render uppercase with no call-site changes. The underlying text, and what screen readers announce, keep their case.
  - The fonts are bundled with the app and do not depend on system fonts.
- **Shapes:** corner radii come from Blackfyre's radius scale.
- A reproducible font build: a `mise` task fetches pinned upstream font files, verifies their checksums and generates the caps cut. The fonts' OFL licences ship with the app.

## Capabilities

### New Capabilities

- `blackfyre-theme`: the Blackfyre theme as the launcher's default, covering colour schemes, bundled typography (including uppercase labels) and shapes.

### Modified Capabilities

None.

## Impact

- `data:themes`:
  - A new bundled kind of `FontFamily`.
  - Blackfyre definitions in new files.
  - In each of `ColorsRepository`, `ShapesRepository` and `TypographyRepository`: one built-in entry.
- `core:preferences`: the defaults of the colour, shape and typography theme IDs in `LauncherSettingsData` become Blackfyre's. Existing installs keep their stored choice.
- `app:ui`: `fontFamilyOf` maps the bundled families to new font resources in `res/font`.
- Tooling: `mise.toml` gains a fonttools tool and a `fonts` task, and a font build script is added under `scripts/`.
- Follow-up changes, not part of this one:
  - `blackfyre-depth`: raised panels, wells and the tilt-driven light.
  - `blackfyre-components`: keycap controls and Blackfyre component styling across the launcher and settings.
- Not affected: the theme database schema, theme import and export for non-Blackfyre themes, the transparency themes.
