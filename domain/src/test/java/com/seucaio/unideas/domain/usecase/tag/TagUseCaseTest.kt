package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
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

/** [TagUseCase] is a delegating facade — these tests only check the delegation itself. */
class TagUseCaseTest {

    private val getTags: GetTagsUseCase = mockk()
    private val addTag: AddTagUseCase = mockk()
    private val renameTag: RenameTagUseCase = mockk()
    private val deleteTag: DeleteTagUseCase = mockk()
    private val useCase = TagUseCase(getTags, addTag, renameTag, deleteTag)

    @Test
    fun `getAll delegates to GetTagsUseCase`() = runTest {
        val tags = TagStub.tags()
        every { getTags() } returns flowOf(tags)

        val result = useCase.getAll().first()

        assertEquals(tags, result)
        verify(exactly = 1) { getTags() }
    }

    @Test
    fun `add delegates to AddTagUseCase`() = runTest {
        coEvery { addTag("urgente") } returns Result.success(1L)

        val result = useCase.add("urgente")

        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { addTag("urgente") }
    }

    @Test
    fun `rename delegates to RenameTagUseCase`() = runTest {
        val tag = TagStub.tag(name = "renomeada")
        coEvery { renameTag(tag) } returns Result.success(Unit)

        val result = useCase.rename(tag)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { renameTag(tag) }
    }

    @Test
    fun `delete delegates to DeleteTagUseCase`() = runTest {
        coEvery { deleteTag(1L) } returns Result.success(DeletionStatus.Deleted)

        val result = useCase.delete(1L)

        assertEquals(DeletionStatus.Deleted, result.getOrNull())
        coVerify(exactly = 1) { deleteTag(1L) }
    }
}
