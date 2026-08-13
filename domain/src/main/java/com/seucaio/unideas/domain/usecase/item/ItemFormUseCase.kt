package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import kotlinx.coroutines.flow.Flow

class ItemFormUseCase(
    private val getItem: GetItemUseCase,
    private val createItem: CreateItemUseCase,
    private val editItem: EditItemUseCase,
    private val deleteItem: DeleteItemUseCase,
) {

    fun get(id: Long): Flow<Item?> = getItem(id)

    suspend fun create(item: Item): Result<Long> = createItem(item)

    suspend fun edit(item: Item): Result<Unit> = editItem(item)

    suspend fun delete(id: Long): Result<Unit> = deleteItem(id)
}
