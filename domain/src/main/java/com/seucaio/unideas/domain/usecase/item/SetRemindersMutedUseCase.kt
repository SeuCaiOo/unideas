package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

class SetRemindersMutedUseCase(
    private val repository: ItemRepository,
    private val reminderRefreshTrigger: ReminderRefreshTrigger,
) : UseCase {

    suspend operator fun invoke(item: Item, muted: Boolean): Result<Item> =
        resultCatching {
            requireNotNull(item.dueDate) { "Item has no dueDate to mute reminders for" }

            val updated = item.copy(remindersMuted = muted)
            repository.updateItem(updated)
            reminderRefreshTrigger.refreshNow()
            updated
        }
}
