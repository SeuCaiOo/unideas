package com.seucaio.unideas.domain.model.outcome

/** Outcome of toggling an [com.seucaio.unideas.domain.model.Item]'s completion. */
sealed interface CompletionResult {

    /** The item (or its current recurring occurrence) was marked completed. */
    data object Completed : CompletionResult

    /** An already-completed item (or occurrence) was marked incomplete again (checkbox toggled off). */
    data object Uncompleted : CompletionResult
}
