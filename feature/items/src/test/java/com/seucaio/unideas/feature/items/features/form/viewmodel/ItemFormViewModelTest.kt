package com.seucaio.unideas.feature.items.features.form.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.domain.usecase.item.EditItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
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
class ItemFormViewModelTest {

    @MockK
    private lateinit var getItem: GetItemUseCase

    @MockK
    private lateinit var getSections: GetSectionsUseCase

    @MockK
    private lateinit var getTags: GetTagsUseCase

    @MockK
    private lateinit var createItem: CreateItemUseCase

    @MockK
    private lateinit var editItem: EditItemUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getSections() } returns flowOf(SectionStub.sections())
        every { getTags() } returns flowOf(TagStub.tags())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long? = null) =
        ItemFormViewModel(itemId, getItem, getSections, getTags, createItem, editItem)

    @Test
    fun `when creating a new item should show blank fields with available sections and tags`() = runTest {
        val vm = viewModel(itemId = null)

        vm.uiState.test {
            val state = awaitItem() as ItemFormUiState.Success
            assertEquals(false, state.isEditing)
            assertEquals("", state.title)
            assertEquals(SectionStub.sections(), state.availableSections)
            assertEquals(TagStub.tags(), state.availableTags)
        }
    }

    @Test
    fun `when editing should load the item fields via GetItemUseCase`() = runTest {
        val item = ItemStub.task(id = 1L, tags = listOf(TagStub.tag(id = 1L)))
        every { getItem(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)

        vm.uiState.test {
            val state = awaitItem() as ItemFormUiState.Success
            assertEquals(true, state.isEditing)
            assertEquals(item.title, state.title)
            assertEquals(item.sectionId, state.sectionId)
            assertEquals(setOf(1L), state.selectedTagIds)
            assertEquals(item.dueDate, state.dueDate)
        }
    }

    @Test
    fun `when the item is not found should emit Error`() = runTest {
        every { getItem(1L) } returns flowOf(null)
        val vm = viewModel(itemId = 1L)

        vm.uiState.test {
            assertEquals(ItemFormUiState.Error(R.string.item_form_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after a load error should retry and succeed`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { getItem(1L) } returnsMany listOf(flowOf(null), flowOf(item))
        val vm = viewModel(itemId = 1L)

        vm.uiState.test {
            assertEquals(ItemFormUiState.Error(R.string.item_form_load_error), awaitItem())
            vm.onEvent(ItemFormEvent.OnRetryClicked)
            val state = awaitItem() as ItemFormUiState.Success
            assertEquals(item.title, state.title)
        }
    }

    @Test
    fun `when OnTitleChanged should update uiState title`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemFormEvent.OnTitleChanged("Nova tarefa"))
            val state = awaitItem() as ItemFormUiState.Success
            assertEquals("Nova tarefa", state.title)
        }
    }

    @Test
    fun `when OnDueDateChanged clears the date should reset recurrence to None`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemFormEvent.OnDueDateChanged(ItemStub.TODAY))
            vm.onEvent(ItemFormEvent.OnRecurrenceChanged(Recurrence.Weekly))
            awaitItem()
            val withRecurrence = awaitItem() as ItemFormUiState.Success
            assertEquals(Recurrence.Weekly, withRecurrence.recurrence)

            vm.onEvent(ItemFormEvent.OnDueDateChanged(null))
            val cleared = awaitItem() as ItemFormUiState.Success
            assertEquals(Recurrence.None, cleared.recurrence)
        }
    }

    @Test
    fun `when OnSaveClicked in create mode should call CreateItemUseCase and navigate back`() = runTest {
        coEvery { createItem(any()) } returns Result.success(10L)
        val vm = viewModel(itemId = null)

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemFormEvent.OnTitleChanged("Nova tarefa"))
        vm.onEvent(ItemFormEvent.OnTagToggled(TagStub.tags().first().id))

        vm.uiAction.test {
            vm.onEvent(ItemFormEvent.OnSaveClicked)
            assertEquals(ItemFormUiAction.NavigateBack, awaitItem())
        }

        coVerify(exactly = 1) {
            createItem(
                match { it.title == "Nova tarefa" && it.tags == listOf(TagStub.tags().first()) },
            )
        }
    }

    @Test
    fun `when OnSaveClicked in edit mode should call EditItemUseCase and navigate back`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { getItem(1L) } returns flowOf(item)
        coEvery { editItem(any()) } returns Result.success(Unit)
        val vm = viewModel(itemId = 1L)

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemFormEvent.OnTitleChanged("Título editado"))

        vm.uiAction.test {
            vm.onEvent(ItemFormEvent.OnSaveClicked)
            assertEquals(ItemFormUiAction.NavigateBack, awaitItem())
        }

        coVerify(exactly = 1) { editItem(match { it.id == 1L && it.title == "Título editado" }) }
    }

    @Test
    fun `when OnSaveClicked with blank title should emit a title-required snackbar`() = runTest {
        coEvery { createItem(any()) } returns Result.failure(IllegalArgumentException("Title is required"))
        val vm = viewModel(itemId = null)

        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemFormEvent.OnSaveClicked)
            assertEquals(ItemFormUiAction.ShowSnackbar(R.string.item_title_required), awaitItem())
        }
    }

    @Test
    fun `when the use case fails unexpectedly should emit ShowError with the exception message`() = runTest {
        coEvery { createItem(any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel(itemId = null)

        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemFormEvent.OnTitleChanged("Nova tarefa"))

        vm.uiAction.test {
            vm.onEvent(ItemFormEvent.OnSaveClicked)
            assertEquals(ItemFormUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when the sections flow throws should fall back to an empty list instead of crashing`() = runTest {
        every { getSections() } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem() as ItemFormUiState.Success
            assertTrue(state.availableSections.isEmpty())
        }
    }
}
