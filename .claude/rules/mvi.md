---
paths:
  - "feature/**/*.kt"
  - "core/ui/**/*.kt"
  - "core/backup/**/*.kt"
---
# MVI / presentation rules

Each screen: `UiState` (sealed: Loading/Success/Error), `UiAction` (one-shot via `Channel(BUFFERED)`, `receiveAsFlow()`), `Event` (user intents), `ViewModel`. Full detail: `docs/CONVENTIONS.md`.

- `uiState` derived via `combine(InternalState, domainFlows)` — no manual `collect` in `init`. **Exception 1:** a screen with no UI-only state (no filter, no selection — e.g. a flat manage-list screen) may derive `uiState` directly from the domain flow (`.map`/`.catch`/`.stateIn`), skipping `combine`/`InternalState` entirely rather than declaring an empty placeholder state. Confirmed for `SectionsViewModel`/`TagsViewModel` (#41/#43) and `AllPrioritiesViewModel` (#28). **Exception 2:** a form whose fields always render (no real screen-level load) doesn't need `Loading`/`Error` in `UiState` at all — just a plain `data class` mutated directly by `onEvent`/a one-shot `init` load; a background-data failure degrades silently (empty list, not a screen error), a real failure (e.g. editing a missing item) is a one-shot `ShowSnackbar` + `NavigateBack`, not a blocking state. Confirmed for `ItemFormViewModel`.
- Use case facades (`SectionUseCase`, `TagUseCase`, `ItemDetailUseCase`, `ItemFormUseCase`, `HomeUseCase`) compose existing single-purpose use cases to cut a ViewModel's constructor param count — one method per operation, each just delegating, never touching a repository directly. Name by entity when one screen uses the full CRUD set (Section/Tag); name by screen when the entity spans several screens with different operation subsets (Item) — a single generic facade covering everything was tried and rejected (no clear owner, hard to find where a method is actually used).
- `combine` is pure — never `_action.send(...)` or suspend inside it. Side effects go in `onEvent` handlers / `LaunchedEffect` in the Screen.
- No manual joins in the ViewModel — data arrives ready from the use case.
- No `.value = ...` (use `.update { it.copy(...) }`); no `(uiState.value as? Success)` cast; no `Context`/`AndroidViewModel` (encapsulate in a repository).
- No hardcoded UI strings — `@StringRes` for snackbars, `e.message.orEmpty()` for errors.
- `:feature:*` depends only on `:domain` + `:core:ui`, never `:data`.
- Reuse `:core:ui` components (TopBar/Loading/Error/Empty/ListItem/dialogs) — don't reimplement inline. Red/amber only for due-date urgency.
- ViewModels are tested (MockK + Turbine) and gated by `koverVerify` since #41 — each `:feature:*` with a tested ViewModel needs the `kover` plugin applied and added to `app/build.gradle.kts`'s aggregation. Composables/Screens stay excluded (no required tests for them yet).
- ViewModel tests: mock fields via `@MockK` + `MockKAnnotations.init(this)` (not inline `= mockk()`). Test names: `` `when <condition/event> should <expected behavior>` `` (no comma, no "given") — since #43.
- Screen: `koinViewModel()` + `collectAsStateWithLifecycle()`; resolve `@StringRes` via `LocalResources.current`; wrap nav callbacks in `rememberUpdatedState`.