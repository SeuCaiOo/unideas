package com.seucaio.unideas.feature.items.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.usecase.item.CompleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.DeleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemDetailUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.feature.items.R
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
class ItemDetailViewModelTest {

    @MockK
    private lateinit var getItemDetail: GetItemDetailUseCase

    @MockK
    private lateinit var getSections: GetSectionsUseCase

    @MockK
    private lateinit var deleteItem: DeleteItemUseCase

    @MockK
    private lateinit var completeItem: CompleteItemUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getSections() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long = 1L) =
        ItemDetailViewModel(itemId, getItemDetail, getSections, deleteItem, completeItem)

    @Test
    fun `when the flow emits an item should update uiState to Success`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { getItemDetail(1L) } returns flowOf(item)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Success(item), awaitItem())
        }
    }

    @Test
    fun `when the item has a section should resolve its name into uiState`() = runTest {
        val item = ItemStub.task(id = 1L, sectionId = 2L)
        every { getItemDetail(1L) } returns flowOf(item)
        every { getSections() } returns flowOf(SectionStub.sections(count = 3))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Success(item, "Seção 2"), awaitItem())
        }
    }

    @Test
    fun `when the item does not exist should emit Error`() = runTest {
        every { getItemDetail(1L) } returns flowOf(null)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Error(R.string.item_detail_load_error), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { getItemDetail(1L) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Error(R.string.item_detail_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { getItemDetail(1L) } returnsMany listOf(flowOf(null), flowOf(item))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Error(R.string.item_detail_load_error), awaitItem())
            vm.onEvent(ItemDetailEvent.OnRetryClicked)
            assertEquals(ItemDetailUiState.Loading, awaitItem())
            assertEquals(ItemDetailUiState.Success(item), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteClicked should show the delete confirmation dialog`() = runTest {
        every { getItemDetail(1L) } returns flowOf(ItemStub.task(id = 1L))
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)

        assertEquals(ItemDetailDialogState.DeleteConfirm, vm.dialogState.value)
    }

    @Test
    fun `when OnDialogDismissed should hide the dialog`() = runTest {
        every { getItemDetail(1L) } returns flowOf(ItemStub.task(id = 1L))
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)
        vm.onEvent(ItemDetailEvent.OnDialogDismissed)

        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteConfirmClicked succeeds should navigate back and hide the dialog`() = runTest {
        every { getItemDetail(1L) } returns flowOf(ItemStub.task(id = 1L))
        coEvery { deleteItem(1L) } returns Unit
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnDeleteConfirmClicked)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }
        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        coVerify(exactly = 1) { deleteItem(1L) }
    }

    @Test
    fun `when OnDeleteConfirmClicked fails should emit ShowError`() = runTest {
        every { getItemDetail(1L) } returns flowOf(ItemStub.task(id = 1L))
        coEvery { deleteItem(1L) } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnDeleteConfirmClicked)
            assertEquals(ItemDetailUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnCompleteClicked for a task should call CompleteItemUseCase`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { getItemDetail(1L) } returns flowOf(item)
        coEvery { completeItem(item, any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        coVerify(exactly = 1) { completeItem(item, any()) }
    }

    @Test
    fun `when OnCompleteClicked for a note should not call CompleteItemUseCase`() = runTest {
        val item = ItemStub.note(id = 1L)
        every { getItemDetail(1L) } returns flowOf(item)
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        coVerify(exactly = 0) { completeItem(any(), any()) }
    }

    @Test
    fun `when OnCompleteClicked fails should emit ShowError`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { getItemDetail(1L) } returns flowOf(item)
        coEvery { completeItem(item, any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnCompleteClicked)
            assertEquals(ItemDetailUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnShareClicked should emit ShareText with the item title`() = runTest {
        val item = ItemStub.task(id = 1L, title = "Pagar contas")
        every { getItemDetail(1L) } returns flowOf(item)
        val vm = viewModel()

        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnShareClicked)
            val action = awaitItem() as ItemDetailUiAction.ShareText
            assertEquals(true, action.text.contains("Pagar contas"))
        }
    }

    @Test
    fun `when OnEditClicked should emit NavigateToEdit with the item id`() = runTest {
        every { getItemDetail(1L) } returns flowOf(ItemStub.task(id = 1L))
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnEditClicked)
            assertEquals(ItemDetailUiAction.NavigateToEdit(1L), awaitItem())
        }
    }
}
