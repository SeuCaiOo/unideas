package com.seucaio.unideas.feature.items.features.detail.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.ItemUseCase
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
    private lateinit var itemUseCase: ItemUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long = 1L) = ItemDetailViewModel(itemId, itemUseCase)

    private fun detailOf(item: Item, sectionName: String? = null) = ItemDetail(item, sectionName)

    @Test
    fun `when the flow emits an item should update uiState to Success`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(item))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Success(item), awaitItem())
        }
    }

    @Test
    fun `when the item has a section should surface its resolved name into uiState`() = runTest {
        val item = ItemStub.task(id = 1L, sectionId = 2L)
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(item, "Seção 2"))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Success(item, "Seção 2"), awaitItem())
        }
    }

    @Test
    fun `when the item does not exist should emit Error`() = runTest {
        every { itemUseCase.getDetail(1L) } returns flowOf(null)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Error(R.string.item_detail_load_error), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { itemUseCase.getDetail(1L) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Error(R.string.item_detail_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemUseCase.getDetail(1L) } returnsMany listOf(flowOf(null), flowOf(detailOf(item)))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemDetailUiState.Error(R.string.item_detail_load_error), awaitItem())
            vm.onEvent(ItemDetailEvent.OnRetryClicked)
            assertEquals(ItemDetailUiState.Success(item), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteClicked should show the delete confirmation dialog`() = runTest {
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(ItemStub.task(id = 1L)))
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)

        assertEquals(ItemDetailDialogState.DeleteConfirm, vm.dialogState.value)
    }

    @Test
    fun `when OnDialogDismissed should hide the dialog`() = runTest {
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(ItemStub.task(id = 1L)))
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)
        vm.onEvent(ItemDetailEvent.OnDialogDismissed)

        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteConfirmClicked succeeds should navigate back and hide the dialog`() = runTest {
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(ItemStub.task(id = 1L)))
        coEvery { itemUseCase.delete(1L) } returns Unit
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnDeleteConfirmClicked)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }
        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        coVerify(exactly = 1) { itemUseCase.delete(1L) }
    }

    @Test
    fun `when OnDeleteConfirmClicked fails should emit ShowError`() = runTest {
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(ItemStub.task(id = 1L)))
        coEvery { itemUseCase.delete(1L) } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.onEvent(ItemDetailEvent.OnDeleteClicked)

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnDeleteConfirmClicked)
            assertEquals(ItemDetailUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnCompleteClicked for a task should call ItemUseCase's complete`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(item))
        coEvery { itemUseCase.complete(item, any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        coVerify(exactly = 1) { itemUseCase.complete(item, any()) }
    }

    @Test
    fun `when OnCompleteClicked for a note should not call ItemUseCase's complete`() = runTest {
        val item = ItemStub.note(id = 1L)
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(item))
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        coVerify(exactly = 0) { itemUseCase.complete(any(), any()) }
    }

    @Test
    fun `when OnCompleteClicked fails should emit ShowError`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(item))
        coEvery { itemUseCase.complete(item, any()) } returns Result.failure(IllegalStateException("boom"))
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
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(item))
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
        every { itemUseCase.getDetail(1L) } returns flowOf(detailOf(ItemStub.task(id = 1L)))
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnEditClicked)
            assertEquals(ItemDetailUiAction.NavigateToEdit(1L), awaitItem())
        }
    }
}
