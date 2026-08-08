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
import java.time.LocalDateTime

/**
 * Toggles a task's completion (checkbox behavior — click again to undo).
 *
 * A recurring task is a **single row whose `dueDate` advances** instead of spawning a new item:
 * completing it records an [ItemCompletionHistory] entry for the occurrence and moves `dueDate` to
 * the recurrence's next date from the item's **original** due date (calendar cadence, independent
 * of when it was actually completed). [Item.completedAt] never gets set for a recurring task, so
 * there is no undo for it — completing is definitive; correcting a past occurrence happens via the
 * history, not the checkbox. A non-recurring task keeps the plain toggle, [Item.completedAt] set or
 * cleared.
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
                item.isCompleted -> {
                    repository.updateItem(item.copy(completedAt = null))
                    CompletionResult.Uncompleted
                }

                item.isRecurring && item.dueDate != null -> {
                    val scheduledDate = item.dueDate
                    historyRepository.insert(
                        ItemCompletionHistory(
                            itemId = item.id,
                            scheduledDate = scheduledDate,
                            completedAt = completedAt
                        ),
                    )
                    val nextDueDate = requireNotNull(item.recurrence.nextDueDate(scheduledDate))
                    repository.updateItem(item.copy(dueDate = nextDueDate))
                    CompletionResult.CompletedAndRenewed(item.id)
                }

                else -> {
                    repository.updateItem(item.copy(completedAt = completedAt))
                    CompletionResult.Completed
                }
            }
            reminderRefreshTrigger.refreshNow()
            result
        }
}
