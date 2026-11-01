# blackfyre-theme Specification

## Purpose

Makes the Blackfyre design system the launcher's default theme: its colours, bundled typefaces and corner radii, applied through the launcher's theme model to every screen.

## Requirements

### Requirement: Blackfyre is the default theme

The launcher SHALL offer Blackfyre as a built-in colour theme, shape theme and typography theme. Each SHALL be the default for a new install. Kvaesitso's own built-in themes and the user's custom themes SHALL remain selectable.

#### Scenario: Fresh install

- **WHEN** the launcher starts for the first time with no stored settings
- **THEN** it uses the Blackfyre colours, shapes and typography

#### Scenario: Existing install

- **WHEN** an install that already stored a theme choice is updated to this version
- **THEN** it keeps its stored choice, and the user can select Blackfyre in the appearance settings

#### Scenario: Switching away and back

- **WHEN** the user selects Kvaesitso's default colour theme in the appearance settings, and later selects Blackfyre again
- **THEN** the launcher uses Kvaesitso's default colours, and then Blackfyre's again

#### Scenario: Built-in themes are read-only

- **WHEN** the user opens the Blackfyre theme in the theme settings
- **THEN** it can be duplicated into an editable custom theme, but not edited or deleted in place, as with the other built-in themes

### Requirement: Colours follow Blackfyre Paper and Dark

The light colour scheme SHALL use Blackfyre's Paper theme and the dark colour scheme its Dark theme, chosen by the launcher's existing light, dark or system setting. Every role SHALL take its value from a Blackfyre colour token in the corresponding theme, as listed in the design's mapping table. No colour SHALL be invented outside the token set.

#### Scenario: Light scheme

- **WHEN** the launcher shows its light scheme
- **THEN** the page background is `bf-paper` (#e6e4df), cards are `bf-surface` (#f3f2ee), and primary text is `bf-ink` (#1b1b19)

#### Scenario: Dark scheme

- **WHEN** the launcher shows its dark scheme
- **THEN** the page background is the Dark `bf-paper` (#1b1b19), cards are the Dark `bf-surface` (#242421), and primary text is the Dark `bf-ink` (#f3f2ee)

### Requirement: Accent contrast rules hold

The colour schemes SHALL never place `bf-accent` (#e2551d) as text or icon colour on a light background. Text and icons drawn in the accent role on light backgrounds SHALL use `bf-accent-text` (#a3390c), and on dark backgrounds `bf-accent-on-dark` (#ff8a4c). Content on an accent fill SHALL use `bf-on-accent` (#1b1b19). Every foreground role SHALL reach a WCAG contrast ratio of at least 4.5:1 against the background role it is paired with. The outline role, which draws control boundaries such as the thumb and border of a switch that is off, SHALL reach at least 3:1 against every surface role. Watch faces drawn on the wallpaper in the accent role SHALL stay visible: in the Dark scheme they SHALL use `bf-accent-on-dark`, not `bf-on-accent`.

#### Scenario: Accent text on light

- **WHEN** a text button or link is drawn in the primary colour on the light scheme's surface
- **THEN** its colour is `bf-accent-text`, not `bf-accent`

#### Scenario: Content on the accent

- **WHEN** content is drawn on the brand orange `bf-accent`
- **THEN** it uses `bf-on-accent`

#### Scenario: Contrast check

- **WHEN** the foreground roles of either scheme are measured against their paired background roles
- **THEN** every pair reaches at least 4.5:1

#### Scenario: Switch that is off

- **WHEN** a switch that is off is drawn on a settings row in either scheme
- **THEN** its thumb and border reach at least 3:1 against the track and the row

#### Scenario: Clock in the Dark scheme

- **WHEN** the launcher is dark, the clock widget uses the theme colour, and its content is light over the wallpaper
- **THEN** the lit parts of the watch face are `bf-accent-on-dark`, not the page colour

### Requirement: Bundled Blackfyre typography

The Blackfyre typography SHALL render reading text (body, title, headline and display styles) in Archivo, with weight 600 for headings, and label styles in JetBrains Mono, tracked out between 0.06em and 0.12em. The fonts SHALL be bundled with the app, so text renders the same whatever fonts the device has.

#### Scenario: Headings and body

- **WHEN** a screen shows a title and body text under the Blackfyre typography
- **THEN** both are set in Archivo, and the title has weight 600

#### Scenario: Device without the fonts

- **WHEN** the device has neither Archivo nor JetBrains Mono installed
- **THEN** the launcher still renders them

### Requirement: Labels render in capitals without changing the text

Label styles under the Blackfyre typography SHALL display lowercase letters, including accented Latin letters, as capitals. The text itself SHALL be unchanged, so screen readers, copy and search use the original case.

#### Scenario: Button label

- **WHEN** a button's label is "Save" under the Blackfyre typography
- **THEN** it is displayed as "SAVE" in JetBrains Mono, and TalkBack announces "Save"

#### Scenario: Accented letters

- **WHEN** a label contains "ő" or "é"
- **THEN** they are displayed as "Ő" and "É"

### Requirement: Shapes follow Blackfyre's radius scale

The Blackfyre shape theme SHALL take every corner radius from Blackfyre's radius tokens (4, 6, 8, 10, 12, 14 and 22 dp), with rounded corners.

#### Scenario: Shape slots

- **WHEN** the Blackfyre shape theme is in effect
- **THEN** each shape slot's radius is one of the Blackfyre radius tokens, as listed in the design

### Requirement: Fonts are reproducible and licensed

The bundled font files SHALL be produced by a repeatable build from pinned upstream sources whose checksums are verified. The OFL licences of Archivo and JetBrains Mono SHALL ship with the app. The derived capitals cut SHALL carry a name of its own.

#### Scenario: Rebuilding the fonts

- **WHEN** the font build task is run on a clean checkout
- **THEN** it produces byte-identical font files, or fails if a source file's checksum does not match
