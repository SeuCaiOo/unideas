package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.dao.ItemLinkDao
import com.seucaio.unideas.data.mapper.toDomain
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemLinkRepositoryImpl(private val dao: ItemLinkDao) : ItemLinkRepository {

    override suspend fun linkItems(itemIdA: Long, itemIdB: Long) = dao.insertLink(itemIdA, itemIdB)

    override suspend fun unlinkItems(itemIdA: Long, itemIdB: Long) = dao.deleteLink(itemIdA, itemIdB)

    override fun getLinkedItems(itemId: Long): Flow<List<Item>> =
        dao.getLinkedItems(itemId).map { rows -> rows.map { it.toDomain() } }
}
