package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.repository.SectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteSectionUseCaseTest {

    private val repository: SectionRepository = mockk()
    private val useCase = DeleteSectionUseCase(repository)

    @Test
    fun `invoke deletes the section when there are no linked items`() = runTest {
        coEvery { repository.countLinkedItems(1L) } returns 0
        coEvery { repository.deleteSection(1L) } returns Unit

        val result = useCase(1L)

        assertEquals(DeletionStatus.Deleted, result.getOrNull())
        coVerify(exactly = 1) { repository.deleteSection(1L) }
    }

    @Test
    fun `invoke blocks deletion when items are linked`() = runTest {
        coEvery { repository.countLinkedItems(1L) } returns 3

        val result = useCase(1L)

        assertEquals(DeletionStatus.BlockedByLinkedItems(3), result.getOrNull())
        coVerify(exactly = 0) { repository.deleteSection(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        coEvery { repository.countLinkedItems(1L) } throws IllegalStateException("boom")

        val result = useCase(1L)

        assertTrue(result.isFailure)
    }
}
