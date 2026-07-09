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
- Tests: DAO (inMemory androidTest), mapper (round-trip), repository (MockK) — `koverVerify` gates coverage.