package com.seucaio.unideas.feature.tags.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import com.seucaio.unideas.feature.tags.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

    private val getTags: GetTagsUseCase = mockk()
    private val addTag: AddTagUseCase = mockk()
    private val renameTag: RenameTagUseCase = mockk()
    private val deleteTag: DeleteTagUseCase = mockk()

    @Before
    fun setUp() {
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
    fun `uiState emits Success with the tags from the flow`() = runTest {
        val tags = TagStub.tags()
        every { getTags() } returns flowOf(tags)
        val vm = TagsViewModel(getTags, addTag, renameTag, deleteTag)

        vm.uiState.test {
            assertEquals(TagsUiState.Success(tags), awaitItem())
        }
    }

    @Test
    fun `uiState emits Error when the flow throws`() = runTest {
        every { getTags() } returns flow { throw IllegalStateException("boom") }
        val vm = TagsViewModel(getTags, addTag, renameTag, deleteTag)

        vm.uiState.test {
            assertEquals(TagsUiState.Error(R.string.tags_load_error), awaitItem())
        }
    }

    @Test
    fun `OnAddClicked happy path calls the use case without emitting an action`() = runTest {
        val vm = viewModel()
        coEvery { addTag.invoke("urgente") } returns Result.success(1L)

        vm.onEvent(TagsEvent.OnAddClicked("urgente"))

        coVerify(exactly = 1) { addTag.invoke("urgente") }
    }

    @Test
    fun `OnAddClicked with blank name emits a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { addTag.invoke("") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.action.test {
            vm.onEvent(TagsEvent.OnAddClicked(""))
            assertEquals(TagsUiAction.ShowSnackbar(R.string.tag_name_required), awaitItem())
        }
    }

    @Test
    fun `OnRenameClicked happy path calls the use case`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag(name = "renomeada")
        coEvery { renameTag.invoke(tag) } returns Result.success(Unit)

        vm.onEvent(TagsEvent.OnRenameClicked(TagStub.tag(), "renomeada"))

        coVerify(exactly = 1) { renameTag.invoke(tag) }
    }

    @Test
    fun `OnRenameClicked with blank name emits a name-required snackbar`() = runTest {
        val vm = viewModel()
        val tag = TagStub.tag(name = "")
        coEvery { renameTag.invoke(tag) } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.action.test {
            vm.onEvent(TagsEvent.OnRenameClicked(TagStub.tag(), ""))
            assertEquals(TagsUiAction.ShowSnackbar(R.string.tag_name_required), awaitItem())
        }
    }

    @Test
    fun `OnDeleteClicked blocked by linked items emits a snackbar with the count`() = runTest {
        val vm = viewModel()
        coEvery { deleteTag.invoke(1L) } returns Result.success(DeletionStatus.BlockedByLinkedItems(3))

        vm.action.test {
            vm.onEvent(TagsEvent.OnDeleteClicked(1L))
            assertEquals(TagsUiAction.ShowSnackbar(R.string.tag_delete_blocked, listOf(3)), awaitItem())
        }
    }

    @Test
    fun `OnDeleteClicked completed does not emit an action`() = runTest {
        val vm = viewModel()
        coEvery { deleteTag.invoke(1L) } returns Result.success(DeletionStatus.Deleted)

        vm.onEvent(TagsEvent.OnDeleteClicked(1L))

        coVerify(exactly = 1) { deleteTag.invoke(1L) }
    }

    @Test
    fun `unexpected repository failure emits ShowError with the exception message`() = runTest {
        val vm = viewModel()
        coEvery { deleteTag.invoke(1L) } returns Result.failure(IllegalStateException("boom"))

        vm.action.test {
            vm.onEvent(TagsEvent.OnDeleteClicked(1L))
            assertEquals(TagsUiAction.ShowError("boom"), awaitItem())
        }
    }
}
