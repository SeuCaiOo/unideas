package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
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

    suspend fun deleteItems(ids: List<Long>): Result<Unit> = runCatching {
        ids.forEach { id -> deleteItemUseCase(id).getOrThrow() }
    }
}
