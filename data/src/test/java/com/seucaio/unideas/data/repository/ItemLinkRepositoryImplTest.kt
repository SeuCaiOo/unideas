package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.dao.ItemLinkDao
import com.seucaio.unideas.data.local.relation.ItemWithTags
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.stub.ItemStub
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

class ItemLinkRepositoryImplTest {

    private val dao: ItemLinkDao = mockk()
    private val repository = ItemLinkRepositoryImpl(dao)

    @Test
    fun `linkItems delegates the exact ids to the dao`() = runTest {
        coEvery { dao.insertLink(1L, 2L) } returns Unit

        repository.linkItems(1L, 2L)

        coVerify(exactly = 1) { dao.insertLink(1L, 2L) }
    }

    @Test
    fun `unlinkItems delegates the exact ids to the dao`() = runTest {
        coEvery { dao.deleteLink(1L, 2L) } returns Unit

        repository.unlinkItems(1L, 2L)

        coVerify(exactly = 1) { dao.deleteLink(1L, 2L) }
    }

    @Test
    fun `getLinkedItems delegates to the dao and maps rows`() = runTest {
        val item = ItemStub.task()
        every { dao.getLinkedItems(1L) } returns flowOf(
            listOf(ItemWithTags(item = item.toEntity(), tags = emptyList())),
        )

        val result = repository.getLinkedItems(1L).first()

        assertEquals(listOf(item), result)
        verify(exactly = 1) { dao.getLinkedItems(1L) }
    }
}
