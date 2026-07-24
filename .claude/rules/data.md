---
paths:
  - "data/**/*.kt"
---
# :data rules

Implements `:domain` contracts with Room. Full detail: `docs/CONVENTIONS.md`; schema in `docs/ARCHITECTURE.md`.

- Entities store dates as `Long` (epoch millis); convert in the mapper (`Long.toLocalDate()` / `LocalDate.toEpochMilli()`). Picker millis use `toLocalDateUtc()`.
- Related data via Room `@Relation`/`@Embedded` — **never join in memory**.
- Mappers are extension functions in `data/mapper/` (Entity ↔ Domain); never leak entities to the UI.
- DAOs return `Flow`. Delete by `id` (`@Query DELETE WHERE id = :id`).
- `UnideasDatabase` is a manual singleton (`@Volatile` + `synchronized`) via `getInstance(context)`.
- **Every `version` bump ships a real `Migration`** (`data/local/database/migration/`, `MIGRATION_X_Y` objects, added via `.addMigrations(...)`) — never `fallbackToDestructiveMigration()`. The app is local-only (Drive backup/restore is manual, opt-in, not automatic sync); a missing migration failing loud (Room throws on open, logged via `logOnError`/Crashlytics) is far safer than one silently deleting every item, section, and tag a user has saved. A build that ships without the right migration is a bug to fix and release, never a reason to erase user data.
- Tests: DAO (inMemory androidTest), mapper (round-trip), repository (MockK) — `koverVerify` gates coverage.