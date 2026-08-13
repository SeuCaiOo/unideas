package com.seucaio.unideas.feature.home.features.priority.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.HomeUseCase
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

/** Each test targets whichever of `itemsState`/`uiState` owns the behavior. */
@OptIn(ExperimentalCoroutinesApi::class)
class PriorityViewModelTest {

    @MockK
    private lateinit var homeUseCase: HomeUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(emptyList())
        every { homeUseCase.hasAnyItem() } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PriorityViewModel(homeUseCase)

    @Test
    fun `when the panel does not exceed the limit should not show the see-all button`() = runTest {
        val items = (1..3).map { ItemStub.overdueTask(id = it.toLong()) }
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(items)
        val vm = viewModel()

        vm.itemsState.test {
            val state = awaitItem()
            assertEquals(items, state.priorityItems)
            assertEquals(false, state.showSeeAllButton)
        }
    }

    @Test
    fun `when the panel exceeds the limit should cap it and show the see-all button`() = runTest {
        val items = (1..6).map { ItemStub.overdueTask(id = it.toLong()) }
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(items)
        val vm = viewModel()

        vm.itemsState.test {
            val state = awaitItem()
            assertEquals(items.take(5), state.priorityItems)
            assertEquals(true, state.showSeeAllButton)
        }
    }

    @Test
    fun `when the priority items query throws should degrade to an empty list, not a screen error`() = runTest {
        every { homeUseCase.getPriorityItems(any(), any()) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.itemsState.test {
            assertEquals(emptyList<Nothing>(), awaitItem().priorityItems)
        }
    }

    @Test
    fun `when the user has no items anywhere should reflect hasAnyItem as false`() = runTest {
        every { homeUseCase.hasAnyItem() } returns flowOf(false)
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem() as PriorityUiState.Success
            assertEquals(false, state.hasAnyItem)
        }
    }

    @Test
    fun `when hasAnyItem throws should emit Error`() = runTest {
        every { homeUseCase.hasAnyItem() } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(PriorityUiState.Error(R.string.home_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        every { homeUseCase.hasAnyItem() } returnsMany
            listOf(flow { throw IllegalStateException("boom") }, flowOf(true))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(PriorityUiState.Error(R.string.home_load_error), awaitItem())
            vm.onEvent(PriorityEvent.OnRetryClicked)
            // Under UnconfinedTestDispatcher, Error -> Loading -> Success runs synchronously;
            // the transient Loading can be overwritten before Turbine observes it (StateFlow
            // conflation), so only the terminal state is asserted here.
            val state = awaitItem() as PriorityUiState.Success
            assertEquals(true, state.hasAnyItem)
        }
    }

    @Test
    fun `when OnItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(PriorityEvent.OnItemClicked(42L))
            assertEquals(PriorityUiAction.NavigateToDetail(42L), awaitItem())
        }
    }

    @Test
    fun `when OnSeeAllClicked should emit NavigateToAllPriorities`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(PriorityEvent.OnSeeAllClicked)
            assertEquals(PriorityUiAction.NavigateToAllPriorities, awaitItem())
        }
    }
}
