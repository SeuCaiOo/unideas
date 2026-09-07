package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import kotlinx.coroutines.flow.Flow

class ItemLinkUseCase(
    private val getLinkedItemsUseCase: GetLinkedItemsUseCase,
    private val linkItemsUseCase: LinkItemsUseCase,
    private val unlinkItemsUseCase: UnlinkItemsUseCase,
) {

    fun getLinkedItems(itemId: Long): Flow<List<Item>> = getLinkedItemsUseCase(itemId)

    suspend fun link(itemIdA: Long, itemIdB: Long): Result<Unit> = linkItemsUseCase(itemIdA, itemIdB)

    suspend fun unlink(itemIdA: Long, itemIdB: Long): Result<Unit> = unlinkItemsUseCase(itemIdA, itemIdB)
}
