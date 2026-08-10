package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.model.outcome.CompletionResult
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
import java.time.LocalTime

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
        val item = ItemStub.task(
            id = 1L,
            tags = listOf(TagStub.tag(id = 1L)),
            dueTime = LocalTime.of(9, 0),
            reminderWarning = ReminderWarning.DaysBefore(1),
        )
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(true, state.isEditing)
            assertEquals(item.title, state.title)
            assertEquals(item.sectionId, state.sectionId)
            assertEquals(setOf(1L), state.selectedTagIds)
            assertEquals(item.dueDate, state.dueDate)
            assertEquals(item.dueTime, state.dueTime)
            assertEquals(item.reminderWarning, state.reminderWarning)
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
    fun `when OnDueDateChanged clears the date should reset recurrence, dueTime and reminderWarning`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemDetailEvent.OnDueDateChanged(ItemStub.TODAY))
            vm.onEvent(ItemDetailEvent.OnRecurrenceChanged(Recurrence.Weekly))
            vm.onEvent(ItemDetailEvent.OnDueTimeChanged(LocalTime.of(14, 0)))
            vm.onEvent(ItemDetailEvent.OnReminderWarningChanged(ReminderWarning.DaysBefore(2)))
            awaitItem()
            awaitItem()
            awaitItem()
            val configured = awaitItem()
            assertEquals(Recurrence.Weekly, configured.recurrence)
            assertEquals(LocalTime.of(14, 0), configured.dueTime)
            assertEquals(ReminderWarning.DaysBefore(2), configured.reminderWarning)

            vm.onEvent(ItemDetailEvent.OnDueDateChanged(null))
            val cleared = awaitItem()
            assertEquals(Recurrence.None, cleared.recurrence)
            assertEquals(null, cleared.dueTime)
            assertEquals(ReminderWarning.None, cleared.reminderWarning)
        }
    }

    @Test
    fun `when OnSaveClicked in create mode should call ItemFormUseCase's create and navigate back`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        coEvery { itemFormUseCase.edit(any()) } returns Result.success(Unit)
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
    fun `when a structured FieldEvent fires with a valid title should auto-save without OnSaveClicked`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        val vm = viewModel(itemId = null)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

        vm.onEvent(ItemDetailEvent.OnTagToggled(TagStub.tags().first().id))

        coVerify(exactly = 1) {
            itemFormUseCase.create(match { it.title == "Nova tarefa" })
        }
    }

    @Test
    fun `when a structured FieldEvent fires with a blank title should not save`() = runTest {
        val vm = viewModel(itemId = null)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnTagToggled(TagStub.tags().first().id))

        coVerify(exactly = 0) { itemFormUseCase.create(any()) }
    }

    @Test
    fun `when OnTitleChanged or OnDescriptionChanged fire alone should not auto-save`() = runTest {
        val vm = viewModel(itemId = null)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
        vm.onEvent(ItemDetailEvent.OnDescriptionChanged("Descrição"))

        coVerify(exactly = 0) { itemFormUseCase.create(any()) }
    }

    @Test
    fun `when a structured FieldEvent fires in edit mode should auto-save via edit`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        coEvery { itemFormUseCase.edit(any()) } returns Result.success(Unit)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnSectionChanged(SectionStub.sections().first().id))

        coVerify(exactly = 1) {
            itemFormUseCase.edit(match { it.id == 1L && it.sectionId == SectionStub.sections().first().id })
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

    @Test
    fun `when OnCompleteClicked on a pending task should complete it directly without a dialog`() = runTest {
        val item = ItemStub.task(id = 1L)
        val completed = item.copy(completedAt = ItemStub.TODAY.atTime(12, 0))
        every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(completed))
        coEvery { itemFormUseCase.complete(any(), any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        assertEquals(true, vm.uiState.value.isCompleted)
        coVerify(exactly = 1) { itemFormUseCase.complete(any(), any()) }
    }

    @Test
    fun `when OnCompleteClicked on a completed task should open the reopen confirmation dialog`() = runTest {
        val item = ItemStub.completedTask(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        assertEquals(ItemDetailDialogState.ReopenConfirm, vm.dialogState.value)
        coVerify(exactly = 0) { itemFormUseCase.complete(any(), any()) }
    }

    @Test
    fun `when OnCompleteConfirmClicked should reopen the task and dismiss the dialog`() = runTest {
        val item = ItemStub.completedTask(id = 1L)
        val reopened = item.copy(completedAt = null)
        every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(reopened))
        coEvery { itemFormUseCase.complete(any(), any()) } returns Result.success(CompletionResult.Uncompleted)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        vm.onEvent(ItemDetailEvent.OnCompleteConfirmClicked)

        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        assertEquals(false, vm.uiState.value.isCompleted)
        coVerify(exactly = 1) { itemFormUseCase.complete(any(), any()) }
    }

    @Test
    fun `when OnDialogDismissed after OnCompleteClicked on a completed task should not reopen it`() = runTest {
        val item = ItemStub.completedTask(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        vm.onEvent(ItemDetailEvent.OnDialogDismissed)

        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        assertEquals(true, vm.uiState.value.isCompleted)
        coVerify(exactly = 0) { itemFormUseCase.complete(any(), any()) }
    }

    @Test
    fun `when completing a recurring task should mark it completed without advancing dueDate`() = runTest {
        val item = ItemStub.task(id = 1L, recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
        val completed = item.copy(lastCompletedScheduledDate = ItemStub.TODAY)
        every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(completed))
        coEvery { itemFormUseCase.complete(any(), any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        assertEquals(true, vm.uiState.value.isCompleted)
        assertEquals(ItemStub.TODAY, vm.uiState.value.dueDate)
    }

    @Test
    fun `when OnCompleteClicked on a completed recurring occurrence should open the reopen confirmation dialog`() =
        runTest {
            val item = ItemStub.task(
                id = 1L,
                recurrence = Recurrence.Weekly,
                dueDate = ItemStub.TODAY,
                lastCompletedScheduledDate = ItemStub.TODAY,
            )
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemDetailEvent.OnCompleteClicked)

            assertEquals(ItemDetailDialogState.ReopenConfirm, vm.dialogState.value)
            coVerify(exactly = 0) { itemFormUseCase.complete(any(), any()) }
        }

    @Test
    fun `when OnHistoryClicked should open the history sheet and load the series history`() = runTest {
        val item = ItemStub.task(id = 1L, recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        every { itemFormUseCase.getHistory(1L) } returns flowOf(emptyList())
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnHistoryClicked)

        assertEquals(ItemDetailDialogState.History, vm.dialogState.value)
        coVerify(exactly = 1) { itemFormUseCase.getHistory(1L) }
    }
}
