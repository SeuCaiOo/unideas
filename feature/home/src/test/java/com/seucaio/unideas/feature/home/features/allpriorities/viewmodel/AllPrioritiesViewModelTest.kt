package com.seucaio.unideas.feature.home.features.allpriorities.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.HomeUseCase
import com.seucaio.unideas.feature.home.R
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
class AllPrioritiesViewModelTest {

    @MockK
    private lateinit var homeUseCase: HomeUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AllPrioritiesViewModel(homeUseCase)

    @Test
    fun `when the flow emits items should update uiState to Success with the full list`() = runTest {
        val items = (1..6).map { ItemStub.overdueTask(id = it.toLong()) }
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(items)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AllPrioritiesUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when the flow emits an empty list should update uiState to Success with an empty list`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AllPrioritiesUiState.Success(emptyList()), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { homeUseCase.getPriorityItems(any(), any()) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AllPrioritiesUiState.Error(R.string.all_priorities_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val items = listOf(ItemStub.overdueTask())
        every { homeUseCase.getPriorityItems(any(), any()) } returnsMany
            listOf(flow { throw IllegalStateException("boom") }, flowOf(items))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AllPrioritiesUiState.Error(R.string.all_priorities_load_error), awaitItem())
            vm.onEvent(AllPrioritiesEvent.OnRetryClicked)
            assertEquals(AllPrioritiesUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when OnItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(AllPrioritiesEvent.OnItemClicked(42L))
            assertEquals(AllPrioritiesUiAction.NavigateToDetail(42L), awaitItem())
        }
    }

    @Test
    fun `when OnCompleteClicked for a known item should call HomeUseCase's complete`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(listOf(item))
        coEvery { homeUseCase.complete(item, any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(AllPrioritiesEvent.OnCompleteClicked(1L))

        coVerify(exactly = 1) { homeUseCase.complete(item, any()) }
    }

    @Test
    fun `when OnCompleteClicked for an unknown item should not call HomeUseCase's complete`() = runTest {
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(AllPrioritiesEvent.OnCompleteClicked(999L))

        coVerify(exactly = 0) { homeUseCase.complete(any(), any()) }
    }

    @Test
    fun `when OnCompleteClicked fails should emit ShowError`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(listOf(item))
        coEvery { homeUseCase.complete(item, any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(AllPrioritiesEvent.OnCompleteClicked(1L))
            assertEquals(AllPrioritiesUiAction.ShowError("boom"), awaitItem())
        }
    }
}
