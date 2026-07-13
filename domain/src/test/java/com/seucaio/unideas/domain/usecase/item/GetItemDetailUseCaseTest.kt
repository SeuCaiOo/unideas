package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetItemDetailUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = GetItemDetailUseCase(repository)

    @Test
    fun `invoke delegates to the repository and emits the item detail`() = runTest {
        val detail = ItemDetail(item = ItemStub.task(sectionId = 2L), sectionName = "Trabalho")
        every { repository.getItemDetail(detail.item.id) } returns flowOf(detail)

        val result = useCase(detail.item.id).first()

        assertEquals(detail, result)
        verify(exactly = 1) { repository.getItemDetail(detail.item.id) }
    }

    @Test
    fun `invoke emits null when the item does not exist`() = runTest {
        every { repository.getItemDetail(99L) } returns flowOf(null)

        val result = useCase(99L).first()

        assertNull(result)
    }
}
