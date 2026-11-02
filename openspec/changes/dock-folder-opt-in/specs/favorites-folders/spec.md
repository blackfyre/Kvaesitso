## MODIFIED Requirements

### Requirement: Pinned tags appear as folders in the dock

The clock widget's dock SHALL show as a folder every pinned tag whose "Show in dock" option is on, before the other dock items, in the order of the Favourites editor's Tags section. Folders SHALL count towards the dock's capacity of grid columns × dock rows; when such folders fill it, the dock SHALL show only folders. A tag that is not pinned, or whose "Show in dock" option is off, SHALL NOT appear in the dock. An item that belongs to a tag shown in the dock SHALL NOT also appear in the dock on its own, whether it is frequently used or pinned; the next item takes its slot. Folders in the dock SHALL look and behave as folders in the Favourites grid do: the same icon, tap, long-press and drag behaviour. The dock SHALL keep showing no labels.

#### Scenario: Folder leads the dock

- **WHEN** tag Home is pinned with "Show in dock" on, Termux and Brave are pinned favourites, and the dock is enabled
- **THEN** the dock starts with the Home folder, followed by Termux and Brave

#### Scenario: Pinned folder without the option stays out

- **WHEN** tags Home and Work are pinned, only Home has "Show in dock" on, and the dock is enabled
- **THEN** the dock shows the Home folder and not the Work folder
- **AND** the Favourites grid shows both folders

#### Scenario: Dock capacity

- **WHEN** the grid has 5 columns, the dock has 1 row, tags Home and Work are pinned with "Show in dock" on, and 6 other items are pinned
- **THEN** the dock shows Home, Work and the first 3 of the other items

#### Scenario: Folder items are not repeated

- **WHEN** tag Home holds Home Assistant and Reolink, is shown in the dock, and Reolink is among the most used apps
- **THEN** the dock shows the Home folder and does not show Reolink on its own
- **AND** the slot Reolink would have taken goes to the next frequently used or pinned item

#### Scenario: Unpinned tag stays out

- **WHEN** tag Home has "Show in dock" on and the user unpins it in the Favourites editor
- **THEN** the Home folder no longer appears in the dock

#### Scenario: Re-pinned tag returns

- **WHEN** tag Home has "Show in dock" on, was unpinned, and the user pins it again
- **THEN** the Home folder appears in the dock again

#### Scenario: Tap in the dock

- **WHEN** tag Home holds Home Assistant, Reolink in that order, is shown in the dock, and the user taps the Home folder there
- **THEN** Home Assistant launches

## ADDED Requirements

### Requirement: Per-folder "Show in dock" option

The tag editor SHALL offer, per tag, a "Show in dock" option. It SHALL be off for a tag that has never had it turned on. Turning it on or off SHALL take effect in the dock when the tag editor saves, without other changes to the tag. The option SHALL follow the tag when the tag is renamed. The option SHALL survive backup and restore; restoring a backup that holds no value for a tag SHALL leave that tag's option off.

#### Scenario: Turn the option on

- **WHEN** tag Work is pinned, its option is off, and the user turns "Show in dock" on in the tag editor and saves
- **THEN** the Work folder appears in the dock

#### Scenario: Turn the option off

- **WHEN** tag Home is shown in the dock and the user turns "Show in dock" off in the tag editor and saves
- **THEN** the Home folder no longer appears in the dock
- **AND** it still appears in the Favourites grid

#### Scenario: Rename keeps the option

- **WHEN** tag Home has "Show in dock" on and the user renames it to House
- **THEN** the House folder appears in the dock

#### Scenario: Backup round trip

- **WHEN** tag Home has "Show in dock" on, the user makes a backup, and restores it
- **THEN** tag Home still has "Show in dock" on

#### Scenario: Older backup

- **WHEN** the user restores a backup made before this option existed
- **THEN** every restored tag has "Show in dock" off

### Requirement: Deleting a tag clears its folder settings

Deleting a tag, from the tag editor or from the tags settings screen, SHALL clear its "Show in dock" option and its custom icon (including "Use first item's icon"). A tag created later with the same name SHALL start with the option off and no custom icon.

#### Scenario: Recreated tag starts clean

- **WHEN** tag Home has "Show in dock" on and a custom icon, the user deletes it, and later creates a new tag named Home
- **THEN** the new Home tag has "Show in dock" off and its folder shows the mini-grid preview
