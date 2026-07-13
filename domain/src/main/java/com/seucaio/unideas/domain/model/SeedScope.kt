package com.seucaio.unideas.domain.model

/** Which debug-only sample dataset [com.seucaio.unideas.domain.usecase.settings.SeedDatabaseUseCase] should insert. */
enum class SeedScope {

    /** Wipes the database, nothing more — tests empty states. */
    EMPTY,

    /** A handful of items, panel under the priority limit — no "See all" button. */
    BASIC,

    /** Exceeds the panel limit, covering every visual case (recurring, completed, sections, tags). */
    FULL,
}
