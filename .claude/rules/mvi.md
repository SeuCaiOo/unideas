---
paths:
  - "feature/**/*.kt"
  - "core/ui/**/*.kt"
  - "core/backup/**/*.kt"
---
# MVI / presentation rules

Each screen: `UiState` (sealed: Loading/Success/Error), `UiAction` (one-shot via `Channel(BUFFERED)`, `receiveAsFlow()`), `Event` (user intents), `ViewModel`. Full detail: `docs/CONVENTIONS.md`.

- `uiState` derived via `combine(InternalState, domainFlows)` — no manual `collect` in `init`.
- `combine` is pure — never `_action.send(...)` or suspend inside it. Side effects go in `onEvent` handlers / `LaunchedEffect` in the Screen.
- No manual joins in the ViewModel — data arrives ready from the use case.
- No `.value = ...` (use `.update { it.copy(...) }`); no `(uiState.value as? Success)` cast; no `Context`/`AndroidViewModel` (encapsulate in a repository).
- No hardcoded UI strings — `@StringRes` for snackbars, `e.message.orEmpty()` for errors.
- `:feature:*` depends only on `:domain` + `:core:ui`, never `:data`.
- Reuse `:core:ui` components (TopBar/Loading/Error/Empty/ListItem/dialogs) — don't reimplement inline. Red/amber only for due-date urgency.
- ViewModels/Composables are excluded from coverage — no required tests for them yet.
- Screen: `koinViewModel()` + `collectAsStateWithLifecycle()`; resolve `@StringRes` via `LocalResources.current`; wrap nav callbacks in `rememberUpdatedState`.