package com.seucaio.unideas.data.repository

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.data.local.dao.ItemDao
import com.seucaio.unideas.data.mapper.toDomain
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Room-backed [ItemRepository]. Tags come already joined by the DAO
 * (`@Relation`); entities never leave this layer.
 */
class ItemRepositoryImpl(
    private val itemDao: ItemDao,
) : ItemRepository {

    override fun getItems(type: ItemType, sectionId: Long?, tagIds: List<Long>): Flow<List<Item>> =
        itemDao.getItems(type, sectionId, tagIds, tagIds.size)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getItemById(id: Long): Flow<Item?> =
        itemDao.getItemById(id).map { it?.toDomain() }

    override fun getPriorityItems(dueOnOrBefore: LocalDate): Flow<List<Item>> =
        itemDao.getPriorityItems(dueOnOrBefore.toEpochMilli())
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun insertItem(item: Item): Long =
        itemDao.insertItemWithTags(item.toEntity(), item.tags.map { it.id })

    override suspend fun updateItem(item: Item) =
        itemDao.updateItemWithTags(item.toEntity(), item.tags.map { it.id })

    override suspend fun deleteItem(id: Long) = itemDao.deleteById(id)
}
