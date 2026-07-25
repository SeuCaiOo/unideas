package com.seucaio.unideas.domain.model.outcome

/**
 * Rich outcome for completing an [com.seucaio.unideas.domain.model.Item],
 * telling the caller whether a recurring instance was spawned.
 */
sealed interface CompletionResult {

    /** The item was marked completed; not recurring, no new instance. */
    data object Completed : CompletionResult

    /** The item was completed and a new instance was created with id [newItemId]. */
    data class CompletedAndRenewed(val newItemId: Long) : CompletionResult

    /** An already-completed item was marked incomplete again (checkbox toggled off). */
    data object Uncompleted : CompletionResult
}
