package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.repository.AutoBackupTrigger
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.usecase.section.SetSectionPinnedUseCase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class HomeUseCase(
    private val getPriorityItemsUseCase: GetPriorityItemsUseCase,
    private val getItemsUseCase: GetItemsUseCase,
    private val completeItemUseCase: CompleteItemUseCase,
    private val hasAnyItemUseCase: HasAnyItemUseCase,
    private val setSectionPinnedUseCase: SetSectionPinnedUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val setItemPinnedUseCase: SetItemPinnedUseCase,
    private val reminderRefreshTrigger: ReminderRefreshTrigger,
    private val autoBackupTrigger: AutoBackupTrigger,
) {

    fun getPriorityItems(today: LocalDate, dueSoonDays: Int): Flow<List<Item>> =
        getPriorityItemsUseCase(today, dueSoonDays)

    fun getItems(type: ItemType, sectionId: Long?, tagIds: List<Long>): Flow<List<Item>> =
        getItemsUseCase(type, sectionId, tagIds)

    suspend fun complete(item: Item, completedAt: LocalDateTime): Result<CompletionResult> =
        completeItemUseCase(item, completedAt)

    fun hasAnyItem(): Flow<Boolean> = hasAnyItemUseCase()

    suspend fun setSectionPinned(id: Long, isPinned: Boolean): Result<Unit> =
        setSectionPinnedUseCase(id, isPinned)

    suspend fun setItemPinned(id: Long, isPinned: Boolean): Result<Unit> =
        setItemPinnedUseCase(id, isPinned)

    suspend fun deleteItems(ids: List<Long>): Result<Unit> = runCatching {
        ids.forEach { id -> deleteItemUseCase(id).getOrThrow() }
    }

    fun refreshReminders() {
        reminderRefreshTrigger.refreshNow()
        autoBackupTrigger.triggerNow()
    }
}
