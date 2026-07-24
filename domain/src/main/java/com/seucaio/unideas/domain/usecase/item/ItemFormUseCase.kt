package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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
