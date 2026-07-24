package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Convenience facade over the single-purpose item use cases the create/edit form needs (kept
 * as-is, still usable on their own) — one method per operation, each just delegating. No
 * repository access here — every call just delegates. Scoped to
 * [com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel], which also now
 * backs the item screen's share/delete/complete actions — merged in from the original read-only
 * detail screen's `ItemDetailUseCase` facade (unrelated to today's `ItemDetailViewModel`) as that
 * screen's editing moved onto this same ViewModel/form layout. That old `ItemDetailUseCase` was
 * later reformulated into the add-item screen, calling [CreateItemUseCase] directly via
 * [com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemViewModel] instead.
 */
class ItemFormUseCase(
    private val getItem: GetItemUseCase,
    private val createItem: CreateItemUseCase,
    private val editItem: EditItemUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val completeItem: CompleteItemUseCase,
) {

    fun get(id: Long): Flow<Item?> = getItem(id)

    suspend fun create(item: Item): Result<Long> = createItem(item)

    suspend fun edit(item: Item): Result<Unit> = editItem(item)

    suspend fun delete(id: Long) = deleteItem(id)

    suspend fun complete(item: Item, completedAt: LocalDateTime): Result<CompletionResult> =
        completeItem(item, completedAt)
}
