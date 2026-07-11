package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Convenience facade over the existing single-purpose item use cases (kept as-is, still usable
 * on their own) — one method per operation, each just delegating. Same shape as
 * [com.seucaio.unideas.domain.usecase.section.SectionUseCase]/[com.seucaio.unideas.domain.usecase.tag.TagUseCase],
 * but scoped only to what [com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailViewModel]
 * needs today (2026-07-11) — unlike Section/Tag, Item's use cases split unevenly across 3
 * screens (list/form/detail), so this isn't a full-CRUD facade; it grows if/when the form
 * screen adopts the same approach. No repository access here — every call just delegates.
 */
class ItemUseCase(
    private val getItemDetail: GetItemDetailUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val completeItem: CompleteItemUseCase,
) {

    fun getDetail(id: Long): Flow<ItemDetail?> = getItemDetail(id)

    suspend fun delete(id: Long) = deleteItem(id)

    suspend fun complete(item: Item, completedAt: LocalDateTime): Result<CompletionResult> =
        completeItem(item, completedAt)
}
