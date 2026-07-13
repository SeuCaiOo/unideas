package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.dao.TagDao
import com.seucaio.unideas.data.mapper.toEntity
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
import org.junit.Test

class TagRepositoryImplTest {

    private val tagDao: TagDao = mockk()
    private val repository = TagRepositoryImpl(tagDao)

    @Test
    fun `getTags maps rows to domain`() = runTest {
        val tags = TagStub.tags(count = 2)
        every { tagDao.getTags() } returns flowOf(tags.map { it.toEntity() })

        val result = repository.getTags().first()

        assertEquals(tags, result)
        verify(exactly = 1) { tagDao.getTags() }
    }

    @Test
    fun `insertTag delegates entity returning the generated id`() = runTest {
        val tag = TagStub.tag(id = 0L)
        coEvery { tagDao.insert(tag.toEntity()) } returns 42L

        val id = repository.insertTag(tag)

        assertEquals(42L, id)
        coVerify(exactly = 1) { tagDao.insert(tag.toEntity()) }
    }

    @Test
    fun `updateTag delegates the entity`() = runTest {
        val tag = TagStub.tag(id = 7L, name = "renomeada")
        coEvery { tagDao.update(tag.toEntity()) } returns Unit

        repository.updateTag(tag)

        coVerify(exactly = 1) { tagDao.update(tag.toEntity()) }
    }

    @Test
    fun `deleteTag delegates the id`() = runTest {
        coEvery { tagDao.deleteById(5L) } returns Unit

        repository.deleteTag(id = 5L)

        coVerify(exactly = 1) { tagDao.deleteById(5L) }
    }

    @Test
    fun `countLinkedItems delegates the id and returns the count`() = runTest {
        coEvery { tagDao.countLinkedItems(5L) } returns 3

        val count = repository.countLinkedItems(tagId = 5L)

        assertEquals(3, count)
        coVerify(exactly = 1) { tagDao.countLinkedItems(5L) }
    }
}
