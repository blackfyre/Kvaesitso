# Spec Delta

## Purpose

Gives each tag a persistent, user-editable order of its items, so that features built on tags, such as folders, have a defined first item and a stable item sequence.

## ADDED Requirements

### Requirement: Tag items have a stored order

Every tag SHALL have a stored order of its items. Every place that lists a tag's items in stored order SHALL return them in that order, skipping items that can no longer be resolved, without changing the order of the remaining items.

#### Scenario: Items are listed in stored order

- **WHEN** a tag's stored order is A, B, C
- **THEN** its items are listed as A, B, C

#### Scenario: Unresolvable item is skipped

- **WHEN** a tag's stored order is A, B, C and item B can no longer be resolved (for example, its app was uninstalled)
- **THEN** its items are listed as A, C

### Requirement: New members join at the end

An item that is added to a tag SHALL be placed after the tag's existing items.

#### Scenario: Item added through the tag editor

- **WHEN** a tag holds A, B and the user adds C in the tag editor and saves
- **THEN** the tag's stored order is A, B, C

#### Scenario: Tag added to an item

- **WHEN** a tag holds A, B and the user adds that tag to item C from C's own customisation sheet or the Favourites editor
- **THEN** the tag's stored order is A, B, C

### Requirement: The user can reorder a tag's items

The tag editor SHALL show the tag's items in stored order and SHALL let the user drag an item to a new position. Saving the tag SHALL store the order shown.

#### Scenario: Reorder and save

- **WHEN** a tag holds A, B, C and the user drags C to the first position in the tag editor and saves
- **THEN** the tag's stored order is C, A, B

#### Scenario: Dismiss without saving

- **WHEN** the user reorders a tag's items in the tag editor and dismisses it without saving
- **THEN** the tag's stored order is unchanged

### Requirement: Other tag edits keep the order

Changes that do not reorder a tag SHALL NOT change the relative order of that tag's items. This covers renaming the tag, changing an item's custom icon or label, adding or removing other tags on an item, and removing an item from the tag.

#### Scenario: Editing an item's tags

- **WHEN** tag X holds A, B, C and the user adds tag Y to B and removes tag Z from B on B's customisation sheet
- **THEN** tag X's stored order is still A, B, C

#### Scenario: Renaming a tag

- **WHEN** tag X holds A, B, C and the user renames X to W
- **THEN** tag W's stored order is A, B, C

#### Scenario: Removing an item

- **WHEN** tag X holds A, B, C and the user removes B from X
- **THEN** tag X's stored order is A, C

### Requirement: Backup and restore preserve the order

A backup SHALL record every tag's stored order, and restoring it SHALL reproduce that order. The backup format version SHALL NOT change, and backups created before this change SHALL still restore.

#### Scenario: Round trip

- **WHEN** tag X holds C, A, B, the user creates a backup, changes the order to A, B, C and then restores the backup
- **THEN** tag X's stored order is C, A, B

#### Scenario: Older backup

- **WHEN** the user restores a backup created by stock Kvaesitso or by this fork before this change
- **THEN** the restore completes, every tag keeps all its items, and each tag's stored order is the order in which its entries appear in the backup
