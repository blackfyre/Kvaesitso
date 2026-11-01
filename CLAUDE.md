# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

A personal fork of Kvaesitso (github.com/MM2-0/Kvaesitso), an Android launcher in Kotlin and Jetpack Compose. The fork is for the owner's use only: not distributed, English only.

Fork goals:

1. **Folders**: tags pinned to the Favourites widget act as home-screen folders.
2. **Blackfyre styling**: restyle the launcher with the Blackfyre design system.

Planning uses OpenSpec (`openspec/`). Put proposals, specs, designs and tasks there, not in ad-hoc planning files.

## Fork constraints

- **Small, localised diff against upstream.** Upstream is merged periodically. Prefer additions in new files and narrow hooks into existing code over edits spread across many files.
- The application ID is `de.mm20.launcher2.blackfyre` (`app/app/build.gradle.kts`), so the fork installs alongside stock Kvaesitso; the debug build is `de.mm20.launcher2.blackfyre.debug`. The per-build-type `app/ui/src/{debug,release,nightly}/res/xml/shortcuts.xml` hard-code the full package and must match it. The Kotlin `namespace` stays `de.mm20.launcher2`.
- The fork is branded "MG-01 Launcher". `app/app/src/{main,debug}/res/values/branding.xml` override `app_name` from `core:base`; all user-visible app-name text goes through that string. The launcher icon (`app/app/src/{main,debug}/res/mipmap-anydpi-v26/ic_launcher.xml`, `mg01_*` drawables and colours, and an overriding `ic_launcher_monochrome`) is the Blackfyre small mark (`favicon.svg` in the design system), not the tree sigil, which the brand book reserves as a watermark. Leave upstream copyright and licence attribution (`AppLicense.kt`, About screen links) intact.
- Packaged resources are restricted to English by `androidResources.localeFilters += "en"` in `app/app/build.gradle.kts`; only the default (en-US) resources ship. Translations from Weblate (`core/i18n/src/main/res/values-*`) stay in the tree, to avoid merge conflicts.
- Weather and other providers that need non-FOSS API keys are out of scope. Do not add keys or repair them.
- The media widget must keep working (`services/music`: media sessions via `MediaController` and the notification listener in `data/notifications`).

## Decisions

1. Blackfyre fidelity: **decided 2026-10-31: theme layer and component layer.** Map Blackfyre onto `data/themes` and also restyle Composables directly (mono labels, keycap controls, depth, a moving light). Keep component restyling in fork-owned files where possible, because `app/ui` has ~900 `MaterialTheme.` references and merges often. Scope is every surface, settings included; the light is driven by device tilt (gravity sensor; overhead under reduced motion); uppercase labels come from a derived caps cut of JetBrains Mono, not call-site `.uppercase()`. Planned as three OpenSpec changes: `blackfyre-theme`, `blackfyre-depth`, `blackfyre-components`.
2. "Home screen" means the Favourites widget and the clock widget's dock (revised 2026-11-01: the owner's home screen uses the dock). If that cannot give an acceptable folder experience, surface it. Do not build a free-placement icon grid.

The Blackfyre design system's source is the Design System artifact https://claude.ai/artifact/JX4X2WDSVT48H94TboBR6V (read `project/README.md`, the brand book, with the Artifact tool's `read` action; it holds `tokens.json`). The site repository `blackfyre/legendary-winner` (local clone `~/Projects/blackfyre/legendary-winner`) mirrors the colour and depth tokens in `assets/css/tokens.css` and ships the fonts in `assets/fonts/` and `static/fonts/`; there is no `tokens/` directory. If neither is reachable, ask for an export. Never reconstruct tokens from memory.

## Toolchain (mise)

`mise.toml` pins the JDK (Temurin 21), the Android command-line tools and `uv` (which runs the font build with its pinned fonttools), sets `ANDROID_HOME`/`ANDROID_SDK_ROOT` to mise's install directory, and puts `platform-tools` (adb) on `PATH`. There is no `local.properties`; the SDK location comes from the environment.

- First-time setup: `mise install`, then `mise run sdk:setup` (installs platform-tools; this writes `$ANDROID_HOME/licenses/android-sdk-license`). AGP then downloads the compile SDK and build-tools itself. `sdkmanager` is deprecated in favour of the bundled `android sdk` CLI but still works.
- Upstream targets bleeding-edge tooling: AGP 9.4, Gradle 9.6, Kotlin 2.4, `compileSdk` 37 with minor API level 1, Room 3 alpha, Compose betas (`gradle/libs.versions.toml`). Upstream officially builds with Android Studio canary.
- Changing the `android-sdk` version in `mise.toml` creates a fresh, empty SDK directory; rerun `mise run sdk:setup`.

## Commands

Run through mise so the pinned environment applies (`mise run <task>` or `mise exec -- ./gradlew …`).

| Purpose | Command |
|---|---|
| Debug APK | `mise run build` (`:app:app:assembleDefaultDebug`) |
| Install on device | `mise run install` (`:app:app:installDefaultDebug`) |
| All unit tests | `mise run test` (`testDebugUnitTest`) |
| Rebuild bundled fonts | `mise run fonts` (needs network; `-- --source-dir <google/fonts checkout>` for offline) |
| One module's tests | `mise exec -- ./gradlew :core:base:testDebugUnitTest` |
| One test class / method | `mise exec -- ./gradlew :core:base:testDebugUnitTest --tests 'de.mm20.launcher2.search.fuzzy.FzfMatcherTest'` (append `.methodName` for one method) |
| Lint | `mise exec -- ./gradlew :app:app:lintDefaultDebug` (`abortOnError = false`, so lint never fails the build) |
| Launch on device | `adb shell am start -W -n de.mm20.launcher2.blackfyre.debug/de.mm20.launcher2.ui.launcher.LauncherActivity` (the `-a MAIN -c LAUNCHER -p …` form fails to resolve on Android 17) |

Build variants: flavours `default` and `fdroid` × build types `debug` (suffix `.debug`), `release` (`.release`, minified), `nightly` (CI only, needs a signing keystore from env). Library modules have no flavours, so their tasks are `…DebugUnitTest`, not `…DefaultDebugUnitTest`.

Unit tests are sparse: only `core/base` (search scoring, fuzzy matcher, opening schedules), `data/locations`, and the fork's `data/customattrs`, `data/themes` and `app/ui` (fork-owned code only) have JVM tests. There are no instrumentation tests in active use. Most behaviour must be verified on a device.

## Architecture

Module layers (see `docs/docs/developer-guide/project-structure/modules.md` for the full list):

- `core/*`: shared contracts and utilities. `core:base` defines the core interfaces (`SavableSearchable`, `Backupable`, searchable types such as `Tag`) that `data` modules implement, so `services` do not depend on `data` directly.
- `data/*`: low-level implementations and persistence. `data:database` is the single Room 3 database (`AppDatabase`).
- `services/*`: business logic per feature (favourites, tags, backup, icons, music, search, widgets…).
- `app:ui`: almost the entire UI, and the only Compose module. `app:app` is just the `Application` class wiring everything together.
- `libs/*`: standalone vendored libraries.

Dependency injection is Koin. Each module has a `Module.kt` declaring its Koin definitions; `app:app` collects them. ViewModels in `app:ui` obtain services via `KoinComponent`/`inject()`.

### Persistence and migrations

- Room schema JSON is exported to `data/database/schemas/de.mm20.launcher2.database.AppDatabase/<version>.json` and must be committed with any schema change.
- Schema version is in `AppDatabase.kt` (`version = …`); hand-written migrations live in `data/database/.../database/migrations/Migration_X_Y.kt` and are registered in `AppDatabase`.
- Tags are stored as rows in `CustomAttributes` (`type = 'tag'`), accessed through `CustomAttrsDao` and `data:customattrs`. A tag's item order (the fork's folder order) is the rows' `id` order (`AUTOINCREMENT`): there is no order column. Every write must append or keep existing rows (`setTags` is a diff; the tag editor rewrites the tag in list order), and backup must export `ORDER BY id` and restore pages in order. Check this whenever an upstream merge touches tag writes or the custom-attribute backup.
- User preferences use AndroidX DataStore (`core:preferences`), not Room.

### Backup and restore

`services:backup` `BackupManager` zips files written by every Koin-registered `Backupable` (e.g. `CustomAttributesRepository`, `SavableSearchableRepository`, `ThemeRepository`, widgets, search actions, settings). Each component writes its own JSON files into the backup directory and reads them back on restore; bulk DB import/export goes through `BackupRestoreDao`. `BackupFormatMajor/Minor` and the format changelog in `BackupManager` govern compatibility: any change to what a component serialises (for example, tag order) needs a minor-format bump and tolerant restore of older backups.

### Favourites, tags and the home screen

- The home screen's widget column (`app/ui/.../launcher/scaffold/components/ClockAndWidgetsHomeComponent.kt`) hosts the Favourites widget; pinned items are managed by `services:favorites`.
- `Tag` (`core/base/.../search/Tag.kt`) is a `SavableSearchable` with `preferDetailsOverLaunch = true`, so `GridItem` opens its popup on tap.
- Tag editing goes through `services:tags` (`TagsService`) and `app/ui/.../launcher/sheets/EditTagSheet*`.
- Folders (pinned tags) are drawn by `GridItem` through the fork's `app/ui/.../search/common/grid/folder/`. Upstream seams to check on every upstream merge: `FavoritesVM` (tags in the grid), `FavoritesPartProvider` (the clock widget's dock; `pinnedTagsFirst`), and the `FirstItemIcon` line in `CustomIcon.fromDatabaseEntity` (`data:customattrs`; the "first item's icon" folder option is stored as the tag's custom icon).
- Fork-only strings go in `core/i18n/src/main/res/values/strings_fork.xml`, not upstream's `strings.xml`.

### Theming

`data:themes` stores user themes in the database as four independent parts — colours, shapes, transparencies and typography — each with its own repository and defaults (`colors/Defaults.kt`, etc.), combined in `ThemeBundle`. `app/ui/.../theme/` turns them into the Compose `MaterialTheme`. `core:devicepose` provides device orientation/location data.

The built-in Blackfyre theme (colours, shapes, typography; `BlackfyreThemeId`) lives in `data/themes/.../themes/blackfyre/`. Its bundled fonts (`app/ui/src/main/res/font/{archivo,jetbrains_mono,bf_mono_caps}.ttf`, OFL texts in `app/ui/src/main/assets/licenses/`) are generated by `mise run fonts` (`scripts/blackfyre_fonts.py`: pinned google/fonts commit, SHA-256 checked, byte-reproducible); regenerate them instead of editing. Upstream seams it touches, to check on every upstream merge:

- `ColorsRepository`, `ShapesRepository`, `TypographyRepository`: one `get()` line and one built-in list entry each.
- `LauncherSettingsData`: the `uiColorsId`, `uiShapesId` and `uiTypographyId` defaults repeat `BlackfyreThemeId` as a literal; `BlackfyreThemeIdTest` keeps them equal.
- `FontFamily` (`data:themes` `typography/Typography.kt`): the `Bundled` kind.
- `app/ui`: `fontFamilyOf` (`theme/typography/Typography.kt`) and `getFontName` (`settings/typography/TypographySettingsScreen.kt`) map `Bundled`; a new `FontFamily` kind upstream breaks these exhaustive `when`s.
- `ClockWidget.kt` `Clock()`: the watch faces are wrapped in the fork's `ClockFaceColors` (two lines; the `when` inside is deliberately not re-indented). Upstream watch faces draw light content in `onPrimaryContainer`, which Blackfyre maps to dark ink on the accent.
- `app/ui/build.gradle.kts`: `testImplementation(libs.bundles.tests)` for fork-owned JVM tests in `app/ui/src/test`.

### Strings

All localisable strings live in `core:i18n` (base language `en-US` in `values/`). UI code references them via `de.mm20.launcher2.ui.R` because `android.nonTransitiveRClass=false`.
