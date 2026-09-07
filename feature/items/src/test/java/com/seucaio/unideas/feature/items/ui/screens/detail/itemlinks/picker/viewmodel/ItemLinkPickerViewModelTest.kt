package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.GetItemsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemLinkUseCase
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
class ItemLinkPickerViewModelTest {

    @MockK
    private lateinit var getItemsUseCase: GetItemsUseCase

    @MockK
    private lateinit var itemLinkUseCase: ItemLinkUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getItemsUseCase(ItemType.TASK) } returns flowOf(emptyList())
        every { itemLinkUseCase.getLinkedItems(1L) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ItemLinkPickerViewModel(1L, ItemType.TASK, getItemsUseCase, itemLinkUseCase)

    @Test
    fun `excludes the current item and already linked items from the list`() = runTest {
        val current = ItemStub.task(id = 1L)
        val linked = ItemStub.task(id = 2L)
        val linkable = ItemStub.task(id = 3L)
        every { getItemsUseCase(ItemType.TASK) } returns flowOf(listOf(current, linked, linkable))
        every { itemLinkUseCase.getLinkedItems(1L) } returns flowOf(listOf(linked))

        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemLinkPickerUiState.Success(items = listOf(linkable)), awaitItem())
        }
    }

    @Test
    fun `when the use case fails should expose an error state`() = runTest {
        every { getItemsUseCase(ItemType.TASK) } returns flow { error("boom") }

        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemLinkPickerUiState.Error(R.string.item_links_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnItemToggled should add and remove the id from selectedIds`() = runTest {
        val item = ItemStub.task(id = 3L)
        every { getItemsUseCase(ItemType.TASK) } returns flowOf(listOf(item))

        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ItemLinkPickerUiState.Success(items = listOf(item)), awaitItem())

            vm.onEvent(ItemLinkPickerEvent.OnItemToggled(3L))
            assertEquals(ItemLinkPickerUiState.Success(items = listOf(item), selectedIds = setOf(3L)), awaitItem())

            vm.onEvent(ItemLinkPickerEvent.OnItemToggled(3L))
            assertEquals(ItemLinkPickerUiState.Success(items = listOf(item)), awaitItem())
        }
    }

    @Test
    fun `when OnConfirmClicked should link every selected item and navigate back`() = runTest {
        val item = ItemStub.task(id = 3L)
        every { getItemsUseCase(ItemType.TASK) } returns flowOf(listOf(item))
        coEvery { itemLinkUseCase.link(1L, 3L) } returns Result.success(Unit)

        val vm = viewModel()
        vm.onEvent(ItemLinkPickerEvent.OnItemToggled(3L))

        vm.uiAction.test {
            vm.onEvent(ItemLinkPickerEvent.OnConfirmClicked)
            assertEquals(ItemLinkPickerUiAction.NavigateBack, awaitItem())
        }
        coVerify(exactly = 1) { itemLinkUseCase.link(1L, 3L) }
    }

    @Test
    fun `when OnConfirmClicked with nothing selected should just navigate back`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(ItemLinkPickerEvent.OnConfirmClicked)
            assertEquals(ItemLinkPickerUiAction.NavigateBack, awaitItem())
        }
        coVerify(exactly = 0) { itemLinkUseCase.link(any(), any()) }
    }

    @Test
    fun `when linking fails should emit ShowError and still navigate back`() = runTest {
        val item = ItemStub.task(id = 3L)
        every { getItemsUseCase(ItemType.TASK) } returns flowOf(listOf(item))
        coEvery { itemLinkUseCase.link(1L, 3L) } returns Result.failure(IllegalStateException("boom"))

        val vm = viewModel()
        vm.onEvent(ItemLinkPickerEvent.OnItemToggled(3L))

        vm.uiAction.test {
            vm.onEvent(ItemLinkPickerEvent.OnConfirmClicked)
            assertEquals(ItemLinkPickerUiAction.ShowError("boom"), awaitItem())
            assertEquals(ItemLinkPickerUiAction.NavigateBack, awaitItem())
        }
    }
}
