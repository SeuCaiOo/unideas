package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TagCrudOperationsTest {

    @MockK
    private lateinit var getTags: GetTagsUseCase

    @MockK
    private lateinit var addTag: AddTagUseCase

    @MockK
    private lateinit var renameTag: RenameTagUseCase

    @MockK
    private lateinit var deleteTag: DeleteTagUseCase

    private lateinit var operations: TagCrudOperations

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        operations = TagCrudOperations(getTags, addTag, renameTag, deleteTag)
    }

    @Test
    fun `getAll delegates to GetTagsUseCase`() = runTest {
        val tags = TagStub.tags()
        every { getTags() } returns flowOf(tags)

        assertEquals(tags, operations.getAll().first())
    }

    @Test
    fun `add delegates to AddTagUseCase`() = runTest {
        coEvery { addTag("urgente") } returns Result.success(1L)

        val result = operations.add("urgente")

        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { addTag("urgente") }
    }

    @Test
    fun `rename copies the new name onto the tag before delegating`() = runTest {
        val tag = TagStub.tag(name = "antigo")
        val renamed = tag.copy(name = "novo")
        coEvery { renameTag(renamed) } returns Result.success(Unit)

        operations.rename(tag, "novo")

        coVerify(exactly = 1) { renameTag(renamed) }
    }

    @Test
    fun `delete delegates to DeleteTagUseCase with the tag id`() = runTest {
        val tag = TagStub.tag(id = 5L)
        coEvery { deleteTag(5L) } returns Result.success(DeletionStatus.Deleted)

        val result = operations.delete(tag)

        assertEquals(DeletionStatus.Deleted, result.getOrNull())
        coVerify(exactly = 1) { deleteTag(5L) }
    }
}
