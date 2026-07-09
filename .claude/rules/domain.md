---
paths:
  - "domain/**/*.kt"
---
# :domain rules

Pure Kotlin — no Android, Room, Compose or Koin. Full detail: `docs/CONVENTIONS.md`.

- Use cases: one responsibility, `operator fun invoke(...)`.
- Delete use cases take `id: Long`, not the domain object.
- Use cases returning `Result<T>`: the **whole body** in `runCatching { ... }` (captures `require` + repository exceptions). Never `Result.success(repo.call())`.
- Complex operations return rich outcomes in `domain/model/outcome/` (e.g. `DeletionStatus.BlockedByLinkedItems(count)`), not loose flags.
- Models use `LocalDate`/`LocalDateTime`, not epoch millis. Repository interfaces live here; implementations in `:data`.
- Every new use case needs a test (happy path + validation failure).