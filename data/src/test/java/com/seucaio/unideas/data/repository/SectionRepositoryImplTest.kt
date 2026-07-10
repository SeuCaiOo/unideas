package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.dao.SectionDao
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.stub.SectionStub
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

class SectionRepositoryImplTest {

    private val sectionDao: SectionDao = mockk()
    private val repository = SectionRepositoryImpl(sectionDao)

    @Test
    fun `getSections maps rows to domain`() = runTest {
        val sections = SectionStub.sections(count = 2)
        every { sectionDao.getSections() } returns flowOf(sections.map { it.toEntity() })

        val result = repository.getSections().first()

        assertEquals(sections, result)
        verify(exactly = 1) { sectionDao.getSections() }
    }

    @Test
    fun `getSectionsByTags delegates tag ids and maps rows to domain`() = runTest {
        val sections = SectionStub.sections(count = 2)
        val tagIds = listOf(1L, 2L)
        every { sectionDao.getSectionsByTags(tagIds) } returns flowOf(sections.map { it.toEntity() })

        val result = repository.getSectionsByTags(tagIds).first()

        assertEquals(sections, result)
        verify(exactly = 1) { sectionDao.getSectionsByTags(tagIds) }
    }

    @Test
    fun `insertSection delegates entity returning the generated id`() = runTest {
        val section = SectionStub.section(id = 0L)
        coEvery { sectionDao.insert(section.toEntity()) } returns 42L

        val id = repository.insertSection(section)

        assertEquals(42L, id)
        coVerify(exactly = 1) { sectionDao.insert(section.toEntity()) }
    }

    @Test
    fun `updateSection delegates the entity`() = runTest {
        val section = SectionStub.section(id = 5L)
        coEvery { sectionDao.update(section.toEntity()) } returns Unit

        repository.updateSection(section)

        coVerify(exactly = 1) { sectionDao.update(section.toEntity()) }
    }

    @Test
    fun `deleteSection delegates the id`() = runTest {
        coEvery { sectionDao.deleteById(5L) } returns Unit

        repository.deleteSection(id = 5L)

        coVerify(exactly = 1) { sectionDao.deleteById(5L) }
    }

    @Test
    fun `countLinkedItems delegates the id and returns the count`() = runTest {
        coEvery { sectionDao.countLinkedItems(5L) } returns 3

        val count = repository.countLinkedItems(sectionId = 5L)

        assertEquals(3, count)
        coVerify(exactly = 1) { sectionDao.countLinkedItems(5L) }
    }
}
