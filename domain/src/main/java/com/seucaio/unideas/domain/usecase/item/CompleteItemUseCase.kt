package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Toggles a task's completion (checkbox behavior — click again to undo).
 *
 * A recurring task is a **single row whose `dueDate` does not move here** — completing it only
 * records an [ItemCompletionHistory] entry for the current occurrence and sets
 * [Item.lastCompletedScheduledDate]; a second click on the same occurrence reopens it (deletes the
 * entry) instead of recording a new one. `dueDate` only advances once that occurrence is actually
 * in the past, via [ProcessMissedOccurrencesUseCase] — never as a side effect of clicking here. A
 * non-recurring task keeps the plain toggle, [Item.completedAt] set or cleared.
 */
class CompleteItemUseCase(
    private val repository: ItemRepository,
    private val historyRepository: ItemCompletionHistoryRepository,
    private val reminderRefreshTrigger: ReminderRefreshTrigger,
) : UseCase {

    suspend operator fun invoke(item: Item, completedAt: LocalDateTime): Result<CompletionResult> =
        resultCatching {
            require(item.type == ItemType.TASK) { "Only tasks can be completed" }

            val result = when {
                item.isRecurring && item.dueDate != null -> toggleOccurrence(item, item.dueDate, completedAt)

                item.isCompleted -> {
                    repository.updateItem(item.copy(completedAt = null))
                    CompletionResult.Uncompleted
                }

                else -> {
                    repository.updateItem(item.copy(completedAt = completedAt))
                    CompletionResult.Completed
                }
            }
            reminderRefreshTrigger.refreshNow()
            result
        }

    private suspend fun toggleOccurrence(
        item: Item,
        scheduledDate: LocalDate,
        completedAt: LocalDateTime,
    ): CompletionResult =
        if (item.isCompleted) {
            historyRepository.deleteOccurrence(item.id, scheduledDate)
            repository.updateItem(item.copy(lastCompletedScheduledDate = null))
            CompletionResult.Uncompleted
        } else {
            historyRepository.insert(
                ItemCompletionHistory(itemId = item.id, scheduledDate = scheduledDate, completedAt = completedAt),
            )
            repository.updateItem(item.copy(lastCompletedScheduledDate = scheduledDate))
            CompletionResult.Completed
        }
}
