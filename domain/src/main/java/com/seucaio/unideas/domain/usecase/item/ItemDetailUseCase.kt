package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Convenience facade over the single-purpose item use cases the read-only detail screen needs
 * (kept as-is, still usable on their own) — one method per operation, each just delegating. No
 * repository access here — every call just delegates. Scoped to
 * [com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailViewModel] only; see
 * [ItemFormUseCase] for the form screen's own subset — Item's use cases split unevenly across
 * screens (unlike Section/Tag, which have one facade covering their single "manage" screen), so
 * naming each facade after the screen it serves keeps it obvious where a method is actually
 * used.
 */
class ItemDetailUseCase(
    private val getItemDetail: GetItemDetailUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val completeItem: CompleteItemUseCase,
) {

    fun getDetail(id: Long): Flow<ItemDetail?> = getItemDetail(id)

    suspend fun delete(id: Long) = deleteItem(id)

    suspend fun complete(item: Item, completedAt: LocalDateTime): Result<CompletionResult> =
        completeItem(item, completedAt)
}
