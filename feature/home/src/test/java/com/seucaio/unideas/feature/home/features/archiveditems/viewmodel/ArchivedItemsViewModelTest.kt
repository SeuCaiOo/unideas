package com.seucaio.unideas.feature.home.features.archiveditems.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.ItemArchiveUseCase
import com.seucaio.unideas.feature.home.R
import io.mockk.MockKAnnotations
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
class ArchivedItemsViewModelTest {

    @MockK
    private lateinit var itemArchiveUseCase: ItemArchiveUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { itemArchiveUseCase.getArchivedItems() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ArchivedItemsViewModel(itemArchiveUseCase)

    @Test
    fun `when the flow emits items should update uiState to Success with the full list`() = runTest {
        val items = listOf(ItemStub.task(id = 1L, status = ItemStatus.ARCHIVED))
        every { itemArchiveUseCase.getArchivedItems() } returns flowOf(items)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ArchivedItemsUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when the flow emits an empty list should update uiState to Success with an empty list`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ArchivedItemsUiState.Success(emptyList()), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { itemArchiveUseCase.getArchivedItems() } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ArchivedItemsUiState.Error(R.string.archived_items_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val items = listOf(ItemStub.task(status = ItemStatus.ARCHIVED))
        every { itemArchiveUseCase.getArchivedItems() } returnsMany
            listOf(flow { throw IllegalStateException("boom") }, flowOf(items))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(ArchivedItemsUiState.Error(R.string.archived_items_load_error), awaitItem())
            vm.onEvent(ArchivedItemsEvent.OnRetryClicked)
            assertEquals(ArchivedItemsUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when OnItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(ArchivedItemsEvent.OnItemClicked(42L))
            assertEquals(ArchivedItemsUiAction.NavigateToDetail(42L), awaitItem())
        }
    }
}
