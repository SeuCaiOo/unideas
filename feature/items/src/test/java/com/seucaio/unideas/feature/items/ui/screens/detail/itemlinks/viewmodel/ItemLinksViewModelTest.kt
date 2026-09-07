package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.stub.ItemStub
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
class ItemLinksViewModelTest {

    @MockK
    private lateinit var itemLinkUseCase: ItemLinkUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { itemLinkUseCase.getLinkedItems(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long?) = ItemLinksViewModel(itemId, itemLinkUseCase)

    @Test
    fun `when itemId is not null should expose the linked items from the use case`() = runTest {
        val items = listOf(ItemStub.task(id = 2L))
        every { itemLinkUseCase.getLinkedItems(1L) } returns flowOf(items)

        val vm = viewModel(1L)

        vm.uiState.test {
            assertEquals(ItemLinksUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when itemId is null should expose an empty success state without calling the use case`() = runTest {
        val vm = viewModel(null)

        vm.uiState.test {
            assertEquals(ItemLinksUiState.Success(), awaitItem())
        }
    }

    @Test
    fun `when the use case fails should expose an error state`() = runTest {
        every { itemLinkUseCase.getLinkedItems(1L) } returns flow { error("boom") }

        val vm = viewModel(1L)

        vm.uiState.test {
            assertEquals(ItemLinksUiState.Error(R.string.item_links_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val items = listOf(ItemStub.task(id = 2L))
        every { itemLinkUseCase.getLinkedItems(1L) } returnsMany
            listOf(flow { throw IllegalStateException("boom") }, flowOf(items))

        val vm = viewModel(1L)

        vm.uiState.test {
            assertEquals(ItemLinksUiState.Error(R.string.item_links_load_error), awaitItem())
            vm.onEvent(ItemLinksEvent.OnRetryClicked)
            assertEquals(ItemLinksUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when OnLinkedItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel(1L)

        vm.uiAction.test {
            vm.onEvent(ItemLinksEvent.OnLinkedItemClicked(2L))
            assertEquals(ItemLinksUiAction.NavigateToDetail(2L), awaitItem())
        }
    }

    @Test
    fun `when OnUnlinkClicked should call unlink with the current item and the other item`() = runTest {
        coEvery { itemLinkUseCase.unlink(1L, 2L) } returns Result.success(Unit)
        val vm = viewModel(1L)

        vm.onEvent(ItemLinksEvent.OnUnlinkClicked(2L))

        coVerify(exactly = 1) { itemLinkUseCase.unlink(1L, 2L) }
    }

    @Test
    fun `when OnUnlinkClicked fails should emit ShowError`() = runTest {
        coEvery { itemLinkUseCase.unlink(1L, 2L) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel(1L)

        vm.uiAction.test {
            vm.onEvent(ItemLinksEvent.OnUnlinkClicked(2L))
            assertEquals(ItemLinksUiAction.ShowError("boom"), awaitItem())
        }
    }
}
