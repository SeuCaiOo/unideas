package com.seucaio.unideas.domain.model.outcome

/**
 * Rich outcome for completing an [com.seucaio.unideas.domain.model.Item],
 * telling the caller whether a recurring instance was spawned.
 */
sealed interface CompletionResult {

    /** The item was marked completed; not recurring, no new instance. */
    data object Completed : CompletionResult

    /** A recurring item ([itemId]) was completed and its `dueDate` advanced to the next occurrence. */
    data class CompletedAndRenewed(val itemId: Long) : CompletionResult

    /** An already-completed item was marked incomplete again (checkbox toggled off). */
    data object Uncompleted : CompletionResult
}
