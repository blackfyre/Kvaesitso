# Proposal

## Why

The Blackfyre theme gives the launcher the brand's colours, type and shapes, but its cards and sheets still use Material's flat elevation. In the brand, depth is part of the identity. One light sits above the device. Panels are raised by a highlight on their top edge and a soft shadow below, and the shadows move away from the light. This change is the second of the three planned in decision 1 (`CLAUDE.md`), after `blackfyre-theme`.

## What Changes

- **Raised panels.** With the Blackfyre colour theme active, the launcher's panels get Blackfyre's panel depth (`bf-depth-panel`, Paper and Dark values) in place of Material's shadow:
  - cards on the home screen and in search results;
  - widgets that have a background;
  - the search bar, unless its style is transparent or hidden;
  - item popups;
  - bottom sheets.
- **Translucent panels.** The shadow is drawn only outside the panel. Translucent cards (the owner uses the Semi-transparent scheme) get depth without a shadow showing through them. Material's shadow is turned off for translucent cards for the same reason.
- **Moving light.** The light follows the device's tilt, read from the gravity sensor:
  - Panels cast their large shadow away from the light, within the brand's offsets.
  - The shadows lag the tilt by about 250 ms.
  - The sensor runs only while the launcher is in the foreground.
  - With animations turned off ("Remove animations"), or without a gravity sensor, the light stays overhead and the sensor is not used.
- **Other themes.** With any other colour theme, the launcher looks and behaves exactly as upstream.

## Capabilities

### New Capabilities

- `blackfyre-depth`: Blackfyre panel depth on the launcher's panels, lit by a light that follows the device's tilt, active only with the Blackfyre colour theme.

### Modified Capabilities

None. The `blackfyre-theme` requirements (colours, type, shapes) are unchanged.

## Impact

- **`app:ui`:**
  - New fork-owned files: the depth modifier, its Paper and Dark values, the light source, and a composition local that carries them.
  - Upstream seams, each a few lines:
    - `LauncherTheme` provides depth when the colour theme is Blackfyre;
    - `LauncherCard` applies it (this covers the cards, widgets, search bar, search results and popups);
    - `BottomSheet` and `DismissableBottomSheet` apply it to the sheet's surface.
  - `CLAUDE.md` lists the new seams.
- **No new dependencies.** The pinned Compose 1.13 provides the shadow drawing; the gravity sensor is read through `SensorManager`.
- **Power:** the gravity sensor is sampled while the launcher is resumed, at the UI rate.
- **Not part of this change:**
  - Plate and well depth (`bf-depth-plate`, `bf-depth-well`), and the sheen on dark plates. Nothing in the launcher is drawn as a dark plate or a well until `blackfyre-components` adds keycaps and tiles, so they move to that change.
  - Settings screens, dialogs (`bf-shadow-dialog`), and the 22 dp panel radius on cards. These are component-layer work.
- **Not affected:** the theme database, backups, the transparency schemes, and the media widget's behaviour.
