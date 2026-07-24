package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailViewModelTest {

    @MockK
    private lateinit var itemFormUseCase: ItemFormUseCase

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

    private fun viewModel(itemId: Long? = null) =
        ItemDetailViewModel(itemId, itemFormUseCase, getSectionsAndTags)

    @Test
    fun `when creating a new item should show blank fields with available sections and tags`() = runTest {
        val vm = viewModel(itemId = null)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isEditing)
            assertEquals("", state.title)
            assertEquals(SectionStub.sections(), state.availableSections)
            assertEquals(TagStub.tags(), state.availableTags)
        }
    }

    @Test
    fun `when editing should load the item fields via ItemFormUseCase's get`() = runTest {
        val item = ItemStub.task(id = 1L, tags = listOf(TagStub.tag(id = 1L)))
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(true, state.isEditing)
            assertEquals(item.title, state.title)
            assertEquals(item.sectionId, state.sectionId)
            assertEquals(setOf(1L), state.selectedTagIds)
            assertEquals(item.dueDate, state.dueDate)
        }
    }

    @Test
    fun `when the item is not found should show a snackbar and mark loadFailed`() = runTest {
        every { itemFormUseCase.get(1L) } returns flowOf(null)
        val vm = viewModel(itemId = 1L)

        vm.uiAction.test {
            assertEquals(ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error), awaitItem())
        }
        assertEquals(true, vm.uiState.value.loadFailed)
    }

    @Test
    fun `when loading the item throws should show a snackbar and mark loadFailed`() = runTest {
        every { itemFormUseCase.get(1L) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel(itemId = 1L)

        vm.uiAction.test {
            assertEquals(ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error), awaitItem())
        }
        assertEquals(true, vm.uiState.value.loadFailed)
    }

    @Test
    fun `when OnRetryClicked after a load failure succeeds should clear loadFailed`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(null), flowOf(item))
        val vm = viewModel(itemId = 1L)

        vm.uiState.test { awaitItem() }
        assertEquals(true, vm.uiState.value.loadFailed)

        vm.onEvent(ItemDetailEvent.OnRetryClicked)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.loadFailed)
            assertEquals(item.title, state.title)
        }
    }

    @Test
    fun `when GetSectionsAndTagsUseCase throws the form still renders with empty reference lists`() = runTest {
        coEvery { getSectionsAndTags() } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isEditing)
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
    fun `when OnSaveClicked in create mode should call ItemFormUseCase's create and navigate back`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        val vm = viewModel(itemId = null)

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
        vm.onEvent(ItemDetailEvent.OnTagToggled(TagStub.tags().first().id))

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }

        coVerify(exactly = 1) {
            itemFormUseCase.create(
                match { it.title == "Nova tarefa" && it.tags == listOf(TagStub.tags().first()) },
            )
        }
    }

    @Test
    fun `when OnSaveClicked in edit mode should call ItemFormUseCase's edit and navigate back`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        coEvery { itemFormUseCase.edit(any()) } returns Result.success(Unit)
        val vm = viewModel(itemId = 1L)

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Título editado"))

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }

        coVerify(exactly = 1) { itemFormUseCase.edit(match { it.id == 1L && it.title == "Título editado" }) }
    }

    @Test
    fun `when OnSaveClicked with blank title should emit a title-required snackbar`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.failure(IllegalArgumentException("Title is required"))
        val vm = viewModel(itemId = null)

        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.ShowSnackbar(R.string.item_title_required), awaitItem())
        }
    }

    @Test
    fun `when the use case fails unexpectedly should emit ShowError with the exception message`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel(itemId = null)

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnSaveClicked)
            assertEquals(ItemDetailUiAction.ShowError("boom"), awaitItem())
        }
    }
}
