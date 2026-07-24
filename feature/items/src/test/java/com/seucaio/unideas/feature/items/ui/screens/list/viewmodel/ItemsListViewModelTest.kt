package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.GetItemsUseCase
import com.seucaio.unideas.feature.items.R
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsListViewModelTest {

    @MockK
    private lateinit var getItems: GetItemsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ItemsListViewModel {
        every { getItems(ItemType.TASK) } returns flowOf(emptyList())
        every { getItems(ItemType.NOTE) } returns flowOf(emptyList())
        return ItemsListViewModel(getItems)
    }

    @Test
    fun `when both flows emit should update uiState to Success with tasks and notes merged by createdAt`() = runTest {
        val task = ItemStub.task(id = 1L, createdAt = LocalDateTime.of(2026, 7, 1, 10, 0))
        val note = ItemStub.note(id = 2L, createdAt = LocalDateTime.of(2026, 7, 2, 10, 0))
        every { getItems(ItemType.TASK) } returns flowOf(listOf(task))
        every { getItems(ItemType.NOTE) } returns flowOf(listOf(note))
        val vm = ItemsListViewModel(getItems)

        vm.uiState.test {
            assertEquals(ItemsListUiState.Success(listOf(note, task)), awaitItem())
        }
    }

    @Test
    fun `when a flow throws should emit Error`() = runTest {
        every { getItems(ItemType.TASK) } returns flow { throw IllegalStateException("boom") }
        every { getItems(ItemType.NOTE) } returns flowOf(emptyList())
        val vm = ItemsListViewModel(getItems)

        vm.uiState.test {
            assertEquals(ItemsListUiState.Error(R.string.items_list_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val task = ItemStub.task(id = 1L)
        every { getItems(ItemType.TASK) } returnsMany listOf(
            flow { throw IllegalStateException("boom") },
            flowOf(listOf(task)),
        )
        every { getItems(ItemType.NOTE) } returns flowOf(emptyList())
        val vm = ItemsListViewModel(getItems)

        vm.uiState.test {
            assertEquals(ItemsListUiState.Error(R.string.items_list_load_error), awaitItem())
            vm.onEvent(ItemsListEvent.OnRetryClicked)
            assertEquals(ItemsListUiState.Loading, awaitItem())
            assertEquals(ItemsListUiState.Success(listOf(task)), awaitItem())
        }
    }

    @Test
    fun `when OnItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(ItemsListEvent.OnItemClicked(1L))
            assertEquals(ItemsListUiAction.NavigateToDetail(1L), awaitItem())
        }
    }

    @Test
    fun `when OnAddClicked should emit NavigateToAddItem`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(ItemsListEvent.OnAddClicked)
            assertEquals(ItemsListUiAction.NavigateToAddItem, awaitItem())
        }
    }
}
