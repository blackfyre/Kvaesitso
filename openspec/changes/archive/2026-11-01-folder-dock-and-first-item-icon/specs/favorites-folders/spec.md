## ADDED Requirements

### Requirement: Pinned tags appear as folders in the dock

The clock widget's dock SHALL show every pinned tag as a folder, before the other dock items, in the order of the Favourites editor's Tags section. Folders SHALL count towards the dock's capacity of grid columns × dock rows; when the pinned tags fill it, the dock SHALL show only folders. A tag that is not pinned SHALL NOT appear in the dock. Folders in the dock SHALL look and behave as folders in the Favourites grid do: the same icon, tap, long-press and drag behaviour. The dock SHALL keep showing no labels.

#### Scenario: Folder leads the dock

- **WHEN** tag Home is pinned, Termux and Brave are pinned favourites, and the dock is enabled
- **THEN** the dock starts with the Home folder, followed by Termux and Brave

#### Scenario: Dock capacity

- **WHEN** the grid has 5 columns, the dock has 1 row, tags Home and Work are pinned, and 6 other items are pinned
- **THEN** the dock shows Home, Work and the first 3 of the other items

#### Scenario: Unpinned tag stays out

- **WHEN** the user unpins tag Home in the Favourites editor
- **THEN** the Home folder no longer appears in the dock

#### Scenario: Tap in the dock

- **WHEN** tag Home holds Home Assistant, Reolink in that order and the user taps the Home folder in the dock
- **THEN** Home Assistant launches

## MODIFIED Requirements

### Requirement: Folder icon previews its contents

A folder's icon SHALL show a mini-grid of the icons of the first items in the tag's stored order, up to four, in the launcher's icon shape. It SHALL update when the tag's items or their order change. If the user has set a custom icon for the tag, the folder SHALL show that icon instead. A tag with no resolvable items SHALL show an empty folder.

The tag editor SHALL offer, per tag, an option to use the first item's icon. When it is on, the folder SHALL show the icon of the first resolvable item in the tag's stored order, as that item is shown elsewhere in the launcher (including its own custom icon and the icon pack), and SHALL follow changes to the order. If the tag has no resolvable items, the folder SHALL show an empty folder. The option and a custom icon are mutually exclusive: choosing an icon or an emoji SHALL turn the option off, and turning the option on SHALL replace the custom icon. Turning the option off SHALL return the folder to the mini-grid. The option SHALL survive backup and restore.

#### Scenario: Preview follows the order

- **WHEN** tag Work holds Mail, Calendar, Docs, Chat, Drive in that order
- **THEN** the Work folder icon shows Mail, Calendar, Docs and Chat

#### Scenario: Reorder updates the preview

- **WHEN** the user moves Drive to the first position of tag Work and saves
- **THEN** the Work folder icon shows Drive, Mail, Calendar and Docs

#### Scenario: Custom icon wins

- **WHEN** the user has chosen a custom icon for tag Work
- **THEN** the Work folder shows that icon, not the mini-grid

#### Scenario: First item's icon

- **WHEN** tag Work holds Mail, Calendar in that order and the user turns on the first item's icon option for Work and saves
- **THEN** the Work folder shows Mail's icon, and other folders still show their mini-grid or custom icon

#### Scenario: First item's icon follows reorders

- **WHEN** the first item's icon option is on for tag Work and the user moves Calendar to the first position and saves
- **THEN** the Work folder shows Calendar's icon

#### Scenario: Picking an icon replaces the option

- **WHEN** the first item's icon option is on for tag Work and the user picks an emoji as Work's icon and saves
- **THEN** the Work folder shows the emoji and the option is off

#### Scenario: Turning the option off

- **WHEN** the first item's icon option is on for tag Work and the user turns it off and saves
- **THEN** the Work folder shows the mini-grid

#### Scenario: Survives backup

- **WHEN** the first item's icon option is on for tag Work and the user backs up and restores the launcher
- **THEN** the Work folder still shows its first item's icon
