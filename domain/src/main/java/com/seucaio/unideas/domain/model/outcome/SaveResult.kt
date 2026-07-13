package com.seucaio.unideas.domain.model.outcome

/**
 * Rich outcome for write operations (create/edit), telling the caller what
 * actually happened instead of a loose flag.
 */
sealed interface SaveResult {

    /** A new record was created with the generated [id]. */
    data class Created(val id: Long) : SaveResult

    /** An existing record (identified by [id]) was updated. */
    data class Updated(val id: Long) : SaveResult
}
