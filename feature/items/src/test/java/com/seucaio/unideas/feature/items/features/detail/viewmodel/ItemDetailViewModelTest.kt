package com.seucaio.unideas.feature.items.features.detail.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.feature.items.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailViewModelTest {

    @MockK
    private lateinit var createItem: CreateItemUseCase

    @MockK
    private lateinit var getSectionsAndTags: GetSectionsAndTagsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { getSectionsAndTags() } returns SectionsAndTags(SectionStub.sections(), TagStub.tags())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ItemDetailViewModel(createItem, getSectionsAndTags)

    @Test
    fun `when created should show blank fields with available sections and tags`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals("", state.title)
            assertEquals(SectionStub.sections(), state.availableSections)
            assertEquals(TagStub.tags(), state.availableTags)
        }
    }

    @Test
    fun `when GetSectionsAndTagsUseCase throws the form still renders with empty reference lists`() = runTest {
        coEvery { getSectionsAndTags() } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state.availableSections.isEmpty())
            assertTrue(state.availableTags.isEmpty())
        }
    }

    @Test
    fun `when OnTitleChanged should update uiState title`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
            val state = awaitItem()
            assertEquals("Nova tarefa", state.title)
        }
    }

    @Test
    fun `when OnDueDateChanged clears the date should reset recurrence to None`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemDetailEvent.OnDueDateChanged(ItemStub.TODAY))
            vm.onEvent(ItemDetailEvent.OnRecurrenceChanged(Recurrence.Weekly))
            awaitItem()
            val withRecurrence = awaitItem()
            assertEquals(Recurrence.Weekly, withRecurrence.recurrence)

            vm.onEvent(ItemDetailEvent.OnDueDateChanged(null))
            val cleared = awaitItem()
            assertEquals(Recurrence.None, cleared.recurrence)
        }
    }

    @Test
    fun `when OnSaveClicked should call CreateItemUseCase and navigate back`() = runTest {
        coEvery { createItem(any()) } returns Result.success(10L)
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
        vm.onEvent(ItemDetailEvent.OnTagToggled(TagStub.tags().first().id))

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }

        coVerify(exactly = 1) {
            createItem(match { it.title == "Nova tarefa" && it.tags == listOf(TagStub.tags().first()) })
        }
    }

    @Test
    fun `when OnSaveClicked with blank title should emit a title-required snackbar`() = runTest {
        coEvery { createItem(any()) } returns Result.failure(IllegalArgumentException("Title is required"))
        val vm = viewModel()

        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.ShowSnackbar(R.string.item_title_required), awaitItem())
        }
    }

    @Test
    fun `when the use case fails unexpectedly should emit ShowError with the exception message`() = runTest {
        coEvery { createItem(any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.ShowError("boom"), awaitItem())
        }
    }
}
