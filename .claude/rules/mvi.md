---
paths:
  - "feature/**/*.kt"
  - "core/ui/**/*.kt"
  - "core/backup/**/*.kt"
---
# MVI / presentation rules

Each screen: `UiState` (sealed: Loading/Success/Error), `UiAction` (one-shot via `Channel(BUFFERED)`, `receiveAsFlow()`), `Event` (user intents), `ViewModel`. Full detail: `docs/CONVENTIONS.md`.

- `uiState` derived via `combine(InternalState, domainFlows)` — no manual `collect` in `init`. **Exception:** a screen with no UI-only state (no filter, no selection — e.g. a flat manage-list screen) may derive `uiState` directly from the domain flow (`.map`/`.catch`/`.stateIn`), skipping `combine`/`InternalState` entirely rather than declaring an empty placeholder state. Confirmed for `SectionsViewModel`/`TagsViewModel` (#41/#43).
- **Simple named-entity (id+name) management screen? Don't write a new ViewModel/Screen.** `SectionsViewModel`/`TagsViewModel`/their Screens were byte-identical copies — generalized into `EntityCrudViewModel<T>` (`:core:common`, `crud` package) + `EntityManagementScreen<T>` (`:core:ui`). A new one of these needs only: an `XCrudOperations : EntityCrudOperations<X>` adapter over the 4 use cases (no shared domain interface required on `X`), a thin `XViewModel : EntityCrudViewModel<X>(...)` subclass (Koin resolves by concrete class — avoids type-erasure collisions), and an `EntityScreenStrings`. See `:feature:sections` for the reference wiring.
- `combine` is pure — never `_action.send(...)` or suspend inside it. Side effects go in `onEvent` handlers / `LaunchedEffect` in the Screen.
- No manual joins in the ViewModel — data arrives ready from the use case.
- No `.value = ...` (use `.update { it.copy(...) }`); no `(uiState.value as? Success)` cast; no `Context`/`AndroidViewModel` (encapsulate in a repository).
- No hardcoded UI strings — `@StringRes` for snackbars, `e.message.orEmpty()` for errors.
- `:feature:*` depends only on `:domain` + `:core:ui`, never `:data`.
- Reuse `:core:ui` components (TopBar/Loading/Error/Empty/ListItem/dialogs) — don't reimplement inline. Red/amber only for due-date urgency.
- ViewModels are tested (MockK + Turbine) and gated by `koverVerify` since #41 — each `:feature:*` with a tested ViewModel needs the `kover` plugin applied and added to `app/build.gradle.kts`'s aggregation. Composables/Screens stay excluded (no required tests for them yet).
- ViewModel tests: mock fields via `@MockK` + `MockKAnnotations.init(this)` (not inline `= mockk()`). Test names: `` `when <condition/event> should <expected behavior>` `` (no comma, no "given") — since #43.
- Screen: `koinViewModel()` + `collectAsStateWithLifecycle()`; resolve `@StringRes` via `LocalResources.current`; wrap nav callbacks in `rememberUpdatedState`.