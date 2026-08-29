package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching
import java.time.LocalDate

/**
 * Advances a recurring [Item] past every scheduled occurrence that fell before [today] without
 * being completed — one missed [ItemCompletionHistory] entry per skipped cycle (`completedAt =
 * null`), `dueDate` landing on the first occurrence that is `today` or later. Called from
 * `ReminderCheckWorker`'s periodic scan; non-recurring items and items whose `dueDate` isn't in
 * the past pass through unchanged.
 */
class ProcessMissedOccurrencesUseCase(
    private val repository: ItemRepository,
    private val historyRepository: ItemCompletionHistoryRepository,
) : UseCase {

    suspend operator fun invoke(item: Item, today: LocalDate): Result<Item> = resultCatching {
        val dueDate = item.dueDate ?: return@resultCatching item
        if (!item.isRecurring || !dueDate.isBefore(today)) return@resultCatching item

        var scheduledDate: LocalDate = dueDate
        var isFirstCycle = true
        while (scheduledDate.isBefore(today)) {
            if (scheduledDate != item.lastCompletedScheduledDate) {
                historyRepository.insert(
                    ItemCompletionHistory(
                        itemId = item.id,
                        scheduledDate = scheduledDate,
                        completedAt = null,
                        originalScheduledDate = if (isFirstCycle) item.pendingExtensionOriginalDueDate else null,
                        extensionCount = if (isFirstCycle) item.pendingExtensionCount else 0,
                    ),
                )
            }
            scheduledDate = requireNotNull(item.recurrence.nextDueDate(scheduledDate))
            isFirstCycle = false
        }

        val advanced = item.copy(
            dueDate = scheduledDate,
            lastCompletedScheduledDate = null,
            pendingExtensionOriginalDueDate = null,
            pendingExtensionCount = 0,
            remindersMuted = false,
        )
        repository.updateItem(advanced)
        advanced
    }
}
