package com.seucaio.unideas.domain.model.outcome

/**
 * Rich outcome for delete operations on entities that items can link to
 * (sections, tags). Deletion is blocked — not silently constrained by FK —
 * when linked items exist, so the UI can tell the user how many.
 */
sealed interface DeletionStatus {

    /** The entity was deleted. */
    data object Deleted : DeletionStatus

    /** Deletion was blocked because [count] items still link to the entity. */
    data class BlockedByLinkedItems(val count: Int) : DeletionStatus
}
