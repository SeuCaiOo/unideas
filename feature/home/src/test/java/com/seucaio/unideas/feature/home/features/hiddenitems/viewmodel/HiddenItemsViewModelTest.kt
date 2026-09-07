package com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.item.GetConfidentialItemsUseCase
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
class HiddenItemsViewModelTest {

    @MockK
    private lateinit var getConfidentialItemsUseCase: GetConfidentialItemsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getConfidentialItemsUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HiddenItemsViewModel(getConfidentialItemsUseCase)

    @Test
    fun `when the flow emits items should update uiState to Success with the full list`() = runTest {
        val items = listOf(ItemStub.task(id = 1L, isConfidential = true))
        every { getConfidentialItemsUseCase() } returns flowOf(items)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(HiddenItemsUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when the flow emits an empty list should update uiState to Success with an empty list`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(HiddenItemsUiState.Success(emptyList()), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { getConfidentialItemsUseCase() } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(HiddenItemsUiState.Error(R.string.hidden_items_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        val items = listOf(ItemStub.task(isConfidential = true))
        every { getConfidentialItemsUseCase() } returnsMany
            listOf(flow { throw IllegalStateException("boom") }, flowOf(items))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(HiddenItemsUiState.Error(R.string.hidden_items_load_error), awaitItem())
            vm.onEvent(HiddenItemsEvent.OnRetryClicked)
            assertEquals(HiddenItemsUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when OnItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HiddenItemsEvent.OnItemClicked(42L))
            assertEquals(HiddenItemsUiAction.NavigateToDetail(42L), awaitItem())
        }
    }
}
