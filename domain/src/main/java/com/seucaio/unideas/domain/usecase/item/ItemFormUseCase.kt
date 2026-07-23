package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Convenience facade over the single-purpose item use cases the create/edit form needs (kept
 * as-is, still usable on their own) — one method per operation, each just delegating. No
 * repository access here — every call just delegates. Scoped to
 * [com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel], which also now
 * backs `ItemDetailScreen`'s share/delete/complete actions — merged in from the old
 * `ItemDetailUseCase` facade as that screen's editing moved onto this same ViewModel/form layout.
 * `ItemDetailUseCase`/[com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailViewModel]
 * were later reformulated into the add-item screen, calling [CreateItemUseCase] directly instead.
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
