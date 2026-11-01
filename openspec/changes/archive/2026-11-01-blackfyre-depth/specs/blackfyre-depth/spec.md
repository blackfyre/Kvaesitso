# Spec Delta

## Purpose

Gives the launcher's panels Blackfyre's physical depth: a highlight on the top edge and a soft shadow below, cast by one light that follows the device's tilt. It applies only while the Blackfyre colour theme is active.

## ADDED Requirements

### Requirement: Depth only with the Blackfyre colour theme
The launcher SHALL draw Blackfyre depth only while the selected colour theme is the built-in Blackfyre theme. With any other colour theme, panels SHALL be drawn as upstream draws them, and the gravity sensor SHALL NOT be used for depth.

#### Scenario: Another colour theme is selected
- **WHEN** the user selects a colour theme other than Blackfyre
- **THEN** cards, widgets, the search bar, popups and sheets show Material's elevation as upstream does
- **AND** no gravity sensor listener is registered for depth

#### Scenario: Switching back to Blackfyre
- **WHEN** the user selects the Blackfyre colour theme again
- **THEN** the panels show Blackfyre depth without restarting the launcher

### Requirement: Launcher panels are raised
With the Blackfyre colour theme, each of the following SHALL be drawn as a raised panel, in place of Material's shadow:
- cards on the home screen and in search results;
- widgets that show a background;
- the search bar in a style that shows a background;
- item popups;
- bottom sheets.

A raised panel SHALL have:
- a 1 dp highlight on its top inner edge, in `bf-highlight` (`#ffffff` in Paper, `#33332f` in Dark);
- a contact shadow: offset 0, 1 dp, blur 2 dp; `rgba(40,36,30,.08)` in Paper, `rgba(0,0,0,.4)` in Dark;
- a cast shadow, blur 30 dp: `rgba(40,36,30,.08)` in Paper, `rgba(0,0,0,.35)` in Dark.

These are `bf-depth-panel`'s values, with CSS pixels taken as dp. A panel with no background SHALL have no depth.

#### Scenario: Card in Paper with the light overhead
- **WHEN** the Blackfyre theme is active in light mode and the light is overhead
- **THEN** a home-screen card shows a white top-edge highlight, a contact shadow, and a cast shadow offset 0 dp horizontally and 10 dp down

#### Scenario: Widget without a background
- **WHEN** an app widget is configured without a background and edit mode is off
- **THEN** the widget shows no highlight and no shadow

#### Scenario: Transparent search bar
- **WHEN** the search bar style is transparent or hidden
- **THEN** the search bar shows no depth

### Requirement: Translucent panels show no shadow through them
A panel's shadows SHALL be drawn only outside the panel's shape. A panel with a translucent background SHALL keep its depth, and no shadow SHALL show through its background.

#### Scenario: Semi-transparent transparency scheme
- **WHEN** the transparency scheme makes cards translucent
- **THEN** the cards show the highlight and the cast shadow around them
- **AND** the area inside each card shows only the card's own colour over the wallpaper

### Requirement: The light follows the device's tilt
The light's position SHALL follow the device's tilt, as if the light were fixed above the device in the world. Each panel's cast shadow SHALL move away from the light:
- The horizontal offset SHALL be `cx × 18 dp`.
- The vertical offset SHALL be `10 dp + cy × 9 dp`.
- `cx` and `cy` SHALL each stay within −1 to 1, and SHALL be 0 when the device lies flat, face up.
- The direction SHALL be expressed on the screen, so that it stays correct when the display is rotated.
- Changes SHALL be smoothed, so that the shadows lag the tilt by about 250 ms. They SHALL NOT jitter while the device is held still.

The highlight and the contact shadow SHALL NOT move.

#### Scenario: Device lying flat
- **WHEN** the device lies flat, face up
- **THEN** cast shadows sit straight below the panels: 0 dp horizontally, 10 dp down

#### Scenario: Device held upright
- **WHEN** the device is held upright in portrait
- **THEN** cast shadows fall further down the screen, up to 19 dp, with no horizontal offset

#### Scenario: Device tilted with its right edge down
- **WHEN** the device is tilted so that its right edge is lower than its left
- **THEN** cast shadows move towards the right edge of the screen

#### Scenario: Quick tilt
- **WHEN** the device is tilted quickly from one side to the other
- **THEN** the shadows follow smoothly, reaching their new position after a short lag rather than jumping

### Requirement: The light stays overhead without motion
The light SHALL stay overhead (`cx = cy = 0`), and the gravity sensor SHALL NOT be used, when either of these holds:
- the system's animations are turned off (Accessibility, "Remove animations", or an animator duration scale of 0);
- the device has no gravity sensor.

#### Scenario: Remove animations is on
- **WHEN** the system's animations are turned off
- **THEN** cast shadows sit straight below the panels whatever the device's tilt
- **AND** no gravity sensor listener is registered

#### Scenario: Device without a gravity sensor
- **WHEN** the device reports no gravity sensor
- **THEN** cast shadows sit straight below the panels and the launcher works normally

### Requirement: The sensor runs only while the launcher is visible
The gravity sensor SHALL be registered only while the launcher is resumed. It SHALL be unregistered when the launcher pauses: another app in front, the screen off, or the launcher stopped. The rate SHALL be no faster than Android's UI sensor rate.

#### Scenario: Another app opens
- **WHEN** the user opens an app from the launcher
- **THEN** the launcher's gravity sensor listener is unregistered

#### Scenario: Returning home
- **WHEN** the user returns to the launcher
- **THEN** the listener is registered again and the shadows follow the tilt
