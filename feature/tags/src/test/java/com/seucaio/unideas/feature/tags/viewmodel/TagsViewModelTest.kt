package com.seucaio.unideas.feature.tags.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import com.seucaio.unideas.feature.tags.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TagsViewModelTest {

    @MockK
    private lateinit var getTags: GetTagsUseCase

    @MockK
    private lateinit var addTag: AddTagUseCase

    @MockK
    private lateinit var renameTag: RenameTagUseCase

    @MockK
    private lateinit var deleteTag: DeleteTagUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): TagsViewModel {
        every { getTags() } returns flowOf(TagStub.tags())
        return TagsViewModel(getTags, addTag, renameTag, deleteTag)
    }

    @Test
    fun `when the flow emits tags should update uiState to Success`() = runTest {
        val tags = TagStub.tags()
        every { getTags() } returns flowOf(tags)
        val vm = TagsViewModel(getTags, addTag, renameTag, deleteTag)

        vm.uiState.test {
            assertEquals(TagsUiState.Success(tags), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { getTags() } returns flow { throw IllegalStateException("boom") }
        val vm = TagsViewModel(getTags, addTag, renameTag, deleteTag)

        vm.uiState.test {
            assertEquals(TagsUiState.Error(R.string.tags_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should re-fetch and update uiState to Success`() = runTest {
        val tags = TagStub.tags()
        every { getTags() } returnsMany listOf(
            flow { throw IllegalStateException("boom") },
            flowOf(tags),
        )
        val vm = TagsViewModel(getTags, addTag, renameTag, deleteTag)

        vm.uiState.test {
            assertEquals(TagsUiState.Error(R.string.tags_load_error), awaitItem())
            vm.onEvent(TagsEvent.OnRetryClicked)
            assertEquals(TagsUiState.Success(tags), awaitItem())
        }
    }

    @Test
    fun `when OnAddClicked should show the Add dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(TagsEvent.OnAddClicked)

        assertEquals(TagsDialogState.Add, vm.dialogState.value)
    }

    @Test
    fun `when OnAddConfirmClicked with a valid name should call the use case and dismiss the dialog`() = runTest {
        val vm = viewModel()
        coEvery { addTag.invoke("urgente") } returns Result.success(1L)

        vm.onEvent(TagsEvent.OnAddClicked)
        vm.onEvent(TagsEvent.OnAddConfirmClicked("urgente"))

        coVerify(exactly = 1) { addTag.invoke("urgente") }
        assertEquals(TagsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnAddConfirmClicked with blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { addTag.invoke("") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.uiAction.test {
            vm.onEvent(TagsEvent.OnAddConfirmClicked(""))
            assertEquals(TagsUiAction.ShowSnackbar(R.string.tag_name_required), awaitItem())
        }
    }

    @Test
    fun `when OnRenameClicked should show the Rename dialog with the tag`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag()

        vm.onEvent(TagsEvent.OnRenameClicked(tag))

        assertEquals(TagsDialogState.Rename(tag), vm.dialogState.value)
    }

    @Test
    fun `when OnRenameConfirmClicked with a valid name should call the use case`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag()
        val renamed = tag.copy(name = "renomeada")
        coEvery { renameTag.invoke(renamed) } returns Result.success(Unit)

        vm.onEvent(TagsEvent.OnRenameClicked(tag))
        vm.onEvent(TagsEvent.OnRenameConfirmClicked("renomeada"))

        coVerify(exactly = 1) { renameTag.invoke(renamed) }
    }

    @Test
    fun `when OnRenameConfirmClicked with blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag()
        val blank = tag.copy(name = "")
        coEvery { renameTag.invoke(blank) } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.onEvent(TagsEvent.OnRenameClicked(tag))

        vm.uiAction.test {
            vm.onEvent(TagsEvent.OnRenameConfirmClicked(""))
            assertEquals(TagsUiAction.ShowSnackbar(R.string.tag_name_required), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteClicked should show the Delete dialog with the tag`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag()

        vm.onEvent(TagsEvent.OnDeleteClicked(tag))

        assertEquals(TagsDialogState.Delete(tag), vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteConfirmClicked is blocked by linked items should emit a snackbar with the count`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag(id = 1L)
        coEvery { deleteTag.invoke(1L) } returns Result.success(DeletionStatus.BlockedByLinkedItems(3))

        vm.onEvent(TagsEvent.OnDeleteClicked(tag))

        vm.uiAction.test {
            vm.onEvent(TagsEvent.OnDeleteConfirmClicked)
            assertEquals(TagsUiAction.ShowSnackbar(R.string.tag_delete_blocked, listOf(3)), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteConfirmClicked completes should not emit an action`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag(id = 1L)
        coEvery { deleteTag.invoke(1L) } returns Result.success(DeletionStatus.Deleted)

        vm.onEvent(TagsEvent.OnDeleteClicked(tag))
        vm.onEvent(TagsEvent.OnDeleteConfirmClicked)

        coVerify(exactly = 1) { deleteTag.invoke(1L) }
    }

    @Test
    fun `when the repository fails unexpectedly should emit ShowError with the exception message`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag(id = 1L)
        coEvery { deleteTag.invoke(1L) } returns Result.failure(IllegalStateException("boom"))

        vm.onEvent(TagsEvent.OnDeleteClicked(tag))

        vm.uiAction.test {
            vm.onEvent(TagsEvent.OnDeleteConfirmClicked)
            assertEquals(TagsUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnDialogDismissed should hide the dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(TagsEvent.OnAddClicked)
        vm.onEvent(TagsEvent.OnDialogDismissed)

        assertEquals(TagsDialogState.None, vm.dialogState.value)
    }
}
