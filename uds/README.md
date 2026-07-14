# unideas-ds

A portable, domain-agnostic Jetpack Compose design system. Every public component takes only
primitives (`String`, `Boolean`, `Int`, `Dp`, `Color`, `ImageVector`, lambdas) or types defined
in this module — never a type from the app that hosts it. That's what makes it safe to drop into
a different app with a different domain model.

## Requirements

- AGP 8.6.1+ / Kotlin 2.0.21+ (compiles unmodified under newer toolchains too — the module's own
  `build.gradle.kts` does not pin plugin versions; it inherits whatever the consuming project's
  root `pluginManagement` declares)
- Compose BOM 2024.10.01 (or newer)
- `minSdk 24`+
- `material-icons-extended` (pulled in transitively by this module)

## Tokens (`theme/`)

| File | Contents |
|---|---|
| `Color.kt` | Semantic color tokens (`Background`, `Surface1/2/3`, `TextPrimary/Secondary/Tertiary`, `Accent`, `Danger`, `Warning`, etc.) — dark theme only by design |
| `Dimens.kt` | `Radii` (corner radii per component), `Spacing`, touch-target constants |
| `Type.kt` | `AppType` text style catalog + `AppTypography` (Material3 `Typography`) |
| `Theme.kt` | `UdsTheme { content }` — wraps `MaterialTheme` with the tokens above |

Every screen that uses this design system must be wrapped in `UdsTheme { ... }`.

## Components (`components/`)

Organized by role, not by the screen that originally used them:

- `buttons/` — `AppIconButton`, `AppFab`, `MiniFabAction`, `SegmentedControl`
- `chips/` — `SelectableChip`, `RemovableChip`, `TextBadge`, `DueBadge`
- `inputs/` — `AppTextField`, `FormField`, `DropdownField`, `FilterDropdownPill`, `DateFieldButton`, `AddEntryRow`
- `lists/` — `ListItemRow` (+ `ListItemUi`), `ManageListRow`, `MetaRow`, `MetaChipsRow`, `ActionRow`, `NavRow`, `GroupHeader`
- `navigation/` — `TabItem`
- `panels/` — `PriorityPanel` (+ `PriorityRowUi`)
- `feedback/` — `AppSnackbarHost`

Components whose visuals depend on data from a domain model (e.g. a task row that needs a title,
a due-date badge, and a checkbox state) take a `*Ui` data class defined in this module
(`ListItemUi`, `PriorityRowUi`) instead of the app's own model. The owning app is responsible for
mapping its domain type to that `Ui` class.

## Hard rules (why this module is portable)

- No `R.*` references and no Android string/plural resources — every user-facing string is a
  parameter.
- No `java.time` usage — dates arrive pre-formatted as `String`.
- No navigation, lifecycle, or Room/database dependencies.
- No imports from the app that hosts this module.

**Exception: `components/legacy/`.** This subfolder holds components ported verbatim from
`unideas`'s old `:core:ui` module (deleted once every consumer moved over) — some of them
(`DeleteConfirmationDialog`, `UnideasEmptyContent`, `UnideasErrorContent`) still take `@StringRes`
params or reference this module's own `R.string.*`, breaking the "no `R.*`" rule above. That's
accepted on purpose: `legacy/` is transitional, not part of the portable surface, and either gets
folded into the rest of `:uds` (converted to plain `String` params) or deleted outright once it's
no longer needed — don't hold it to the same bar as the rest of the module.

Every component file that plausibly needs one has a `@Preview @Composable private fun ...`
wrapped in `UdsTheme` with hard-coded sample data — treat these previews as the living catalog.

## Porting to another project

1. Copy the `uds/` folder into the target project's root.
2. In the target's `settings.gradle.kts`: `include(":uds")`.
3. In the consuming module's `build.gradle.kts`: `implementation(project(":uds"))`.
4. Build. Because the module's own `build.gradle.kts` doesn't pin AGP/Kotlin/Compose-compiler
   versions, it compiles against whatever the target project's root already declares — no version
   reconciliation needed as long as the target's Compose BOM is >= 2024.10.01 and minSdk >= 24.

To verify the module hasn't picked up an app-specific dependency before porting, run from the
repo root:

```
grep -rln "R\.[a-z]" uds/src/main/java/ # expect no output
grep -rn "java.time" uds/src/           # expect no output
```
