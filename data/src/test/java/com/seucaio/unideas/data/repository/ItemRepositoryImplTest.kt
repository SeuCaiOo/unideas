package com.seucaio.unideas.data.repository

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.data.local.dao.ItemDao
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.data.local.relation.ItemWithTags
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.TagStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemRepositoryImplTest {

    private val itemDao: ItemDao = mockk()
    private val repository = ItemRepositoryImpl(itemDao)

    @Test
    fun `getItems delegates filters to dao and maps rows to domain`() = runTest {
        val item = ItemStub.task(sectionId = 3L, tags = TagStub.tags(count = 2))
        val row = ItemWithTags(
            item = item.toEntity(),
            tags = item.tags.map { TagEntity(id = it.id, name = it.name) },
        )
        val tagIds = listOf(1L, 2L)
        every { itemDao.getItems(ItemType.TASK, 3L, tagIds, 2) } returns flowOf(listOf(row))

        val result = repository.getItems(type = ItemType.TASK, sectionId = 3L, tagIds = tagIds).first()

        assertEquals(listOf(item), result)
        verify(exactly = 1) { itemDao.getItems(ItemType.TASK, 3L, tagIds, 2) }
    }

    @Test
    fun `getItemById maps the row and emits null when missing`() = runTest {
        val item = ItemStub.note()
        every { itemDao.getItemById(item.id) } returns flowOf(
            ItemWithTags(item = item.toEntity(), tags = emptyList()),
        )
        every { itemDao.getItemById(99L) } returns flowOf(null)

        assertEquals(item, repository.getItemById(item.id).first())
        assertNull(repository.getItemById(99L).first())
        verify(exactly = 1) { itemDao.getItemById(item.id) }
        verify(exactly = 1) { itemDao.getItemById(99L) }
    }

    @Test
    fun `getPriorityItems converts threshold to epoch millis and maps rows`() = runTest {
        val threshold = ItemStub.TODAY.plusDays(3)
        val item = ItemStub.overdueTask()
        every { itemDao.getPriorityItems(threshold.toEpochMilli()) } returns flowOf(
            listOf(ItemWithTags(item = item.toEntity(), tags = emptyList())),
        )

        val result = repository.getPriorityItems(dueOnOrBefore = threshold).first()

        assertEquals(listOf(item), result)
        verify(exactly = 1) { itemDao.getPriorityItems(threshold.toEpochMilli()) }
    }

    @Test
    fun `insertItem delegates entity and tag ids returning the generated id`() = runTest {
        val item = ItemStub.task(id = 0L, tags = TagStub.tags(count = 2))
        coEvery { itemDao.insertItemWithTags(item.toEntity(), listOf(1L, 2L)) } returns 42L

        val id = repository.insertItem(item)

        assertEquals(42L, id)
        coVerify(exactly = 1) { itemDao.insertItemWithTags(item.toEntity(), listOf(1L, 2L)) }
    }

    @Test
    fun `updateItem delegates entity and tag ids`() = runTest {
        val item = ItemStub.task(id = 7L, tags = TagStub.tags(count = 1))
        coEvery { itemDao.updateItemWithTags(item.toEntity(), listOf(1L)) } returns Unit

        repository.updateItem(item)

        coVerify(exactly = 1) { itemDao.updateItemWithTags(item.toEntity(), listOf(1L)) }
    }

    @Test
    fun `deleteItem delegates the id to the dao`() = runTest {
        coEvery { itemDao.deleteById(7L) } returns Unit

        repository.deleteItem(id = 7L)

        coVerify(exactly = 1) { itemDao.deleteById(7L) }
    }
}
