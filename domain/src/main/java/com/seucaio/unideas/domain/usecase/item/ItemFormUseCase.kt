package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import kotlinx.coroutines.flow.Flow

/**
 * Convenience facade over the single-purpose item use cases the create/edit form needs (kept
 * as-is, still usable on their own) — one method per operation, each just delegating. No
 * repository access here — every call just delegates. Scoped to
 * [com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel] only; see
 * [ItemDetailUseCase] for the detail screen's own subset.
 */
class ItemFormUseCase(
    private val getItem: GetItemUseCase,
    private val createItem: CreateItemUseCase,
    private val editItem: EditItemUseCase,
) {

    fun get(id: Long): Flow<Item?> = getItem(id)

    suspend fun create(item: Item): Result<Long> = createItem(item)

    suspend fun edit(item: Item): Result<Unit> = editItem(item)
}
