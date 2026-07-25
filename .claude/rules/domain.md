---
paths:
  - "domain/**/*.kt"
---
# :domain rules

Pure Kotlin — no Android, Room, Compose or Koin. Full detail: `docs/CONVENTIONS.md`. **One documented
exception:** `implementation(libs.timber)`, used only by `domain/util/ResultLogging.kt` — see below.

- Use cases: one responsibility, `operator fun invoke(...)`.
- Delete use cases take `id: Long`, not the domain object.
- Use cases returning `Result<T>`: the **whole body** in `resultCatching("UseCaseName") { ... }` (from `domain/util/ResultLogging.kt`) instead of raw `runCatching` — captures `require` + repository exceptions, and Timber-logs anything that isn't an `IllegalArgumentException` (validation misses aren't bugs). Never `Result.success(repo.call())`.
- Use cases that just expose a repository `Flow` directly (queries with no other logic) chain `.logOnError("UseCaseName")` (same file) before returning — logs then rethrows, so a caller's own `.catch` still degrades the UI exactly as before, but the failure now leaves a trace (Logcat in debug, Crashlytics in release via `CrashlyticsTree`). Centralized here, not in each ViewModel, since every use case already funnels through this one choke point — a `runCatching`/`.catch` scattered per-ViewModel silently swallows exceptions with no log anywhere.
- Complex operations return rich outcomes in `domain/model/outcome/` (e.g. `DeletionStatus.BlockedByLinkedItems(count)`), not loose flags.
- Models use `LocalDate`/`LocalDateTime`, not epoch millis. Repository interfaces live here; implementations in `:data`.
- Every new use case needs a test (happy path + validation failure).
- A use case facade (composing other use cases, one method per operation, no repository access) is fine when it only reduces a ViewModel's param count — never let it grow real logic of its own; that belongs in the single-purpose use cases it delegates to.