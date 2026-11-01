# blackfyre-components Specification

## Purpose

Gives the launcher Blackfyre's physical controls and surfaces when the Blackfyre colour theme is active: keycap buttons, chips and toggles; dialogs; panel corners; settings panels; wells; and mono numbers. With any other theme, everything stays as upstream draws it.

## Requirements

### Requirement: Components only with the Blackfyre colour theme
Every requirement in this capability SHALL apply only while the selected colour theme is the built-in Blackfyre theme. With any other colour theme, the affected controls and surfaces SHALL look and behave as upstream's Material 3 versions do.

#### Scenario: Another colour theme
- **WHEN** the user selects a colour theme other than Blackfyre
- **THEN** buttons, chips, toggles, floating action buttons, dialogs, launcher cards, settings categories, calculator and unit converter results and the digital clock faces look as upstream draws them

#### Scenario: Switching back
- **WHEN** the user selects the Blackfyre colour theme again
- **THEN** the Blackfyre components return without restarting the launcher

### Requirement: Buttons are keycaps
With Blackfyre, the following SHALL be drawn as keycaps with 10 dp corners and a minimum height of 44 dp, with labels in the button label style (mono capitals):
- filled buttons, filled icon buttons and floating action buttons (all sizes) as the **primary key**: `bf-accent` (#e2551d) with `bf-on-accent` (#1b1b19) content, a 2 dp bottom edge in `bf-accent-edge` (#a93a0f), and a 1 dp top highlight of white at 25% opacity;
- tonal buttons, tonal icon buttons and outlined buttons as the **secondary key**: `bf-paper` with `bf-ink` content, a 2 dp bottom edge in `bf-line-strong` and a 1 dp top highlight in `bf-highlight`.

The secondary key's colours SHALL follow the theme:
- Paper: #e6e4df, #1b1b19, edge #bfbbb3, highlight #ffffff;
- Dark: #1b1b19, #f3f2ee, edge #0e0e0d, highlight #33332f.

When a call site passes its own container and content colours, the key SHALL use them, with a bottom edge of the container colour darkened.

#### Scenario: Primary button in Paper
- **WHEN** a dialog or screen shows a filled button with the Blackfyre theme in light mode
- **THEN** it is orange with dark text, a darker orange edge below it, and 10 dp corners

#### Scenario: Tonal button in Dark
- **WHEN** a tonal button shows with the Blackfyre theme in dark mode
- **THEN** it is #1b1b19 with light text, a near-black edge below and a #33332f highlight on its top edge

### Requirement: Keys sink when pressed
While a key is pressed, its face SHALL move down by its edge's height (2 dp), and the edge SHALL NOT show. The face SHALL return when the press ends or is cancelled. Under reduced motion (animations off), the change SHALL be immediate. A disabled key SHALL draw at 38% opacity, without an edge, and SHALL NOT respond to presses.

#### Scenario: Press and release
- **WHEN** the user presses and holds a keycap button
- **THEN** its face sits 2 dp lower with no edge below it
- **AND** on release it returns to rest and the button's action runs once

#### Scenario: Disabled key
- **WHEN** a keycap button is disabled
- **THEN** it shows faded, without an edge, and a tap does nothing

### Requirement: Chips and toggles are keys
With Blackfyre, the following SHALL be drawn as secondary keys with 8 dp corners and a minimum height of 32 dp, with labels in the `micro` style (JetBrains Mono capitals, 10 sp, tracked 0.12 em):
- filter, assist and input chips;
- toggle buttons.

A selected filter chip, and a checked toggle button, SHALL be drawn as a key held down: lowered by its edge, with no edge, in `bf-pressed` with `bf-on-pressed` content. That is #1b1b19 with #f3f2ee in Paper, and #3a3935 with #f3f2ee in Dark. Selection and checked state SHALL stay exposed to accessibility services as before.

#### Scenario: Selected filter chip
- **WHEN** the user selects a filter chip in the search filters
- **THEN** the chip shows as a pressed dark key with light text
- **AND** a screen reader announces it as selected

#### Scenario: Unselected chip
- **WHEN** a chip is not selected
- **THEN** it shows as a raised light key with a micro capitals label

### Requirement: Material controls that already fit stay Material
With Blackfyre, text buttons, standard icon buttons, switches, sliders, checkboxes and radio buttons SHALL keep their Material 3 rendering in the Blackfyre colour scheme and type.

#### Scenario: Text button in a dialog
- **WHEN** a dialog shows confirm and dismiss text buttons
- **THEN** they render as Material text buttons in the accent text colour and mono capitals, without a key

### Requirement: Dialogs are Blackfyre panels
With Blackfyre, alert dialogs SHALL have:
- 22 dp corners;
- a `bf-surface` background with no tonal tint;
- `bf-shadow-dialog`: a 1 dp ring in #33332f, and a shadow offset 20 dp down, blurred 60 dp, of black at 50%.

#### Scenario: Delete confirmation
- **WHEN** a settings screen asks to confirm a deletion
- **THEN** the dialog shows with 22 dp corners, a thin dark ring, and a deep soft shadow

### Requirement: Launcher cards take the panel radius
With Blackfyre, launcher cards SHALL default to 22 dp corners (`bf-radius-panel`) in place of the Material medium shape. This covers search results, widgets, the search bar and item popups. A call site that passes its own shape SHALL keep it.

#### Scenario: Widget card
- **WHEN** a widget with a background shows on the home screen
- **THEN** its card has 22 dp corners

### Requirement: Settings categories are panels
With Blackfyre, each settings category's group of preferences SHALL be drawn as a raised panel: 22 dp corners and Blackfyre panel depth. Its title SHALL be set in the kicker style (mono capitals, 11 sp, tracked 0.1 em) in muted ink.

#### Scenario: Appearance settings
- **WHEN** the user opens Appearance settings
- **THEN** each group of preferences sits in a rounded raised panel under a small mono capitals title

### Requirement: Results sit in wells in mono numbers
With Blackfyre, the calculator's result and each unit converter result value SHALL be shown in a well:
- an inset shadow of 0 dp, 2 dp, blur 5 dp, black at 55%;
- a 1 dp inner ring in black;
- the dark well ground `bf-ink-code` (#121211 in Paper, #0b0b0a in Dark);
- light content `bf-on-panel` (#f3f2ee);
- numbers in JetBrains Mono.

The well SHALL stay lit from above: it SHALL NOT move with the device's tilt.

#### Scenario: Calculator result
- **WHEN** the user types `12*7` in search
- **THEN** the result 84 shows in a dark inset well in JetBrains Mono, with light text

#### Scenario: Tilting with a well on screen
- **WHEN** the device is tilted while a well is shown
- **THEN** the panels' cast shadows move and the well's inset shadow does not

### Requirement: Digital clock faces use mono digits
With Blackfyre, the digital clock faces SHALL draw their digits in JetBrains Mono, at the sizes upstream uses. The digits SHALL be tabular, so that changing digits do not shift the layout.

#### Scenario: Minute change
- **WHEN** the time changes from 11:11 to 11:12 on a digital clock face
- **THEN** the digits are JetBrains Mono and the time's width does not change

### Requirement: Swapped call sites stay swapped
The project SHALL include a check that fails when a file in the launcher UI uses a Material control that this capability replaces, instead of the fork's equivalent. It SHALL also include a script that rewrites such call sites, changing only their imports.

#### Scenario: Upstream adds a Material button
- **WHEN** an upstream merge adds a file that imports Material's `Button`
- **THEN** the unit test run fails and names the file
- **AND** running the script rewrites the import, after which the test passes
