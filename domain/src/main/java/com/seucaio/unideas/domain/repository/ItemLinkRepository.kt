package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.Item
import kotlinx.coroutines.flow.Flow

/** Contract for symmetric item↔item links. Implemented in `:data` (Room). */
interface ItemLinkRepository {

    /** Links [itemIdA] and [itemIdB]; a no-op if they're already linked, either direction. */
    suspend fun linkItems(itemIdA: Long, itemIdB: Long)

    /** Removes the link between [itemIdA] and [itemIdB], if any, regardless of argument order. */
    suspend fun unlinkItems(itemIdA: Long, itemIdB: Long)

    /** Observes every item linked to [itemId], from either side of the link. */
    fun getLinkedItems(itemId: Long): Flow<List<Item>>
}
