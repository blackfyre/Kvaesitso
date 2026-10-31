# Spec Delta

## Purpose

Turns pinned tags into home-screen folders in the Favourites grid: a folder icon that previews its contents, launches its first item on tap and opens on long-press.

## ADDED Requirements

### Requirement: Pinned tags appear as folders in the Favourites grid

When no tag filter is selected, the Favourites grid SHALL show every pinned tag as a folder icon, labelled with the tag's name (or its custom label), before the other favourites, in the order of the Favourites editor's Tags section. The tag chip row above the grid SHALL keep its current behaviour. When a tag filter is selected, the grid SHALL show that tag's items as it does today, without folder icons.

#### Scenario: Folders lead the grid

- **WHEN** tags Work and Games are pinned in that order in the Favourites editor and no tag filter is selected
- **THEN** the Favourites grid starts with the folders Work and Games, followed by the other favourites

#### Scenario: Chip row unchanged

- **WHEN** tag Work is pinned
- **THEN** the chip row above the grid still shows a Work chip, and tapping it filters the grid to Work's items

#### Scenario: Tag filter selected

- **WHEN** the user selects the Work chip
- **THEN** the grid shows Work's items and no folder icons

#### Scenario: Unpinned tag

- **WHEN** the user unpins tag Work in the Favourites editor
- **THEN** the Work folder no longer appears in the grid

### Requirement: Folder icon previews its contents

A folder's icon SHALL show a mini-grid of the icons of the first items in the tag's stored order, up to four, in the launcher's icon shape. It SHALL update when the tag's items or their order change. If the user has set a custom icon for the tag, the folder SHALL show that icon instead. A tag with no resolvable items SHALL show an empty folder.

#### Scenario: Preview follows the order

- **WHEN** tag Work holds Mail, Calendar, Docs, Chat, Drive in that order
- **THEN** the Work folder icon shows Mail, Calendar, Docs and Chat

#### Scenario: Reorder updates the preview

- **WHEN** the user moves Drive to the first position of tag Work and saves
- **THEN** the Work folder icon shows Drive, Mail, Calendar and Docs

#### Scenario: Custom icon wins

- **WHEN** the user has chosen a custom icon for tag Work
- **THEN** the Work folder shows that icon, not the mini-grid

### Requirement: Tap launches the first item

A tap on a folder SHALL launch the first item in the tag's stored order that launches successfully, and SHALL record that launch for that item as a launch from the Favourites grid would. If no item launches, the tap SHALL open the folder.

#### Scenario: Tap launches the first item

- **WHEN** tag Work holds Mail, Calendar and the user taps the Work folder
- **THEN** Mail launches

#### Scenario: Empty folder

- **WHEN** tag Work has no resolvable items and the user taps the Work folder
- **THEN** the Work folder opens

### Requirement: Long-press opens the folder

A long-press on a folder SHALL give haptic feedback and open the folder from the icon: a popup that shows the tag's name and its items as a grid in stored order. Tapping an item in the popup SHALL launch it. The popup SHALL offer an action that opens the tag editor for that tag. Back, or a tap outside the popup, SHALL close it.

#### Scenario: Open and launch

- **WHEN** the user long-presses the Work folder and taps Calendar in the popup
- **THEN** Calendar launches

#### Scenario: Edit from the folder

- **WHEN** the user long-presses the Work folder and chooses the edit action
- **THEN** the tag editor opens for tag Work

#### Scenario: Close

- **WHEN** the folder popup is open and the user presses back
- **THEN** the popup closes and nothing launches

### Requirement: Folder gestures do not take over scrolling or scaffold gestures

A drag that starts on a folder icon SHALL behave exactly as a drag that starts on any other favourite does: it scrolls the widget column, and it triggers whatever the launcher does for that drag elsewhere in the grid. A folder SHALL NOT open on a drag.

#### Scenario: Vertical drag on a folder

- **WHEN** the user drags vertically starting on a folder icon
- **THEN** the widget column scrolls and no folder opens

#### Scenario: Horizontal drag on a folder

- **WHEN** the user swipes horizontally starting on a folder icon
- **THEN** the result is the same as for the same swipe starting on another favourite in the grid, and no folder opens
