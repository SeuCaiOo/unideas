package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
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
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
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
    private lateinit var itemOccurrenceUseCase: ItemOccurrenceUseCase

    @MockK
    private lateinit var getSectionsAndTags: GetSectionsAndTagsUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        coEvery { getSectionsAndTags() } returns SectionsAndTags(
            SectionStub.sections(),
            TagStub.tags()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        itemId: Long? = null,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) =
        ItemDetailViewModel(
            itemId = itemId,
            itemFormUseCase = itemFormUseCase,
            itemOccurrenceUseCase = itemOccurrenceUseCase,
            getSectionsAndTags = getSectionsAndTags,
            savedStateHandle = savedStateHandle
        )

    @Test
    fun `when creating a new item should show blank fields with available sections and tags`() =
        runTest {
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
            assertEquals(
                ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error),
                awaitItem()
            )
        }
        assertEquals(true, vm.uiState.value.loadFailed)
    }

    @Test
    fun `when loading the item throws should show a snackbar and mark loadFailed`() = runTest {
        every { itemFormUseCase.get(1L) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel(itemId = 1L)

        vm.uiAction.test {
            assertEquals(
                ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error),
                awaitItem()
            )
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
    fun `when GetSectionsAndTagsUseCase throws the form still renders with empty reference lists`() =
        runTest {
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
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
            val state = awaitItem()
            assertEquals("Nova tarefa", state.title)
        }
    }

    @Test
    fun `when OnDueDateChanged clears the date should reset recurrence, dueTime and reminderWarning`() =
        runTest {
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
    fun `when a structured FieldEvent fires with a valid title should auto-save immediately`() =
        runTest {
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
    fun `when the ViewModel is cleared with a pending debounce should flush it synchronously`() {
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        val store = ViewModelStore()
        val vm = viewModel(itemId = null)
        store.put("key", vm)
        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

        store.clear()

        coVerify(exactly = 1) { itemFormUseCase.create(match { it.title == "Nova tarefa" }) }
    }

    @Test
    fun `when the ViewModel is cleared with no pending debounce should not save again`() {
        val item = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val store = ViewModelStore()
        store.put("key", viewModel(itemId = 1L))

        store.clear()

        coVerify(exactly = 0) { itemFormUseCase.edit(any()) }
    }

    @Test
    fun `when OnTitleChanged fires should not save before the debounce window elapses`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        val vm = viewModel(itemId = null)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
        testDispatcher.scheduler.advanceTimeBy(200)

        coVerify(exactly = 0) { itemFormUseCase.create(any()) }
    }

    @Test
    fun `when OnTitleChanged and OnDescriptionChanged fire in sequence should debounce into a single save`() =
        runTest {
            coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
            val vm = viewModel(itemId = null)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
            vm.onEvent(ItemDetailEvent.OnDescriptionChanged("Descrição"))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) {
                itemFormUseCase.create(match { it.title == "Nova tarefa" && it.description == "Descrição" })
            }
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
            itemFormUseCase.edit(
                match {
                    it.id == 1L && it.sectionId == SectionStub.sections().first().id
                }
            )
        }
    }

    @Test
    fun `when OnBackRequested with a valid title in edit mode should flush the pending save and navigate back`() =
        runTest {
            val item = ItemStub.task(id = 1L)
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            coEvery { itemFormUseCase.edit(any()) } returns Result.success(Unit)
            val vm = viewModel(itemId = 1L)

            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnTitleChanged("Título editado"))

            vm.uiAction.test {
                vm.onEvent(ItemDetailEvent.OnBackRequested)
                assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
            }

            coVerify(exactly = 1) { itemFormUseCase.edit(match { it.id == 1L && it.title == "Título editado" }) }
        }

    @Test
    fun `when the use case fails unexpectedly should emit ShowError with the exception message`() =
        runTest {
            coEvery { itemFormUseCase.create(any()) } returns Result.failure(IllegalStateException("boom"))
            val vm = viewModel(itemId = null)

            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

            vm.uiAction.test {
                vm.onEvent(ItemDetailEvent.OnBackRequested)
                assertEquals(ItemDetailUiAction.ShowError("boom"), awaitItem())
            }
        }

    @Test
    fun `when OnCompleteClicked on a pending task should complete it directly without a dialog`() =
        runTest {
            val item = ItemStub.task(id = 1L)
            val completed = item.copy(completedAt = ItemStub.TODAY.atTime(12, 0))
            every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(completed))
            coEvery { itemOccurrenceUseCase.complete(any(), any(), any()) } returns Result.success(
                CompletionResult.Completed
            )
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemDetailEvent.OnCompleteClicked)

            assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
            assertEquals(true, vm.uiState.value.isCompleted)
            coVerify(exactly = 1) { itemOccurrenceUseCase.complete(any(), any(), any()) }
        }

    @Test
    fun `when OnCompleteClicked on a completed task should open the reopen confirmation dialog`() =
        runTest {
            val item = ItemStub.completedTask(id = 1L)
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemDetailEvent.OnCompleteClicked)

            assertEquals(ItemDetailDialogState.ReopenConfirm, vm.dialogState.value)
            coVerify(exactly = 0) { itemOccurrenceUseCase.complete(any(), any(), any()) }
        }

    @Test
    fun `when OnCompleteClicked completes a task should emit the completed snackbar`() = runTest {
        val item = ItemStub.task(id = 1L)
        val completed = item.copy(completedAt = ItemStub.TODAY.atTime(12, 0))
        every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(completed))
        coEvery { itemOccurrenceUseCase.complete(any(), any(), any()) } returns Result.success(
            CompletionResult.Completed
        )
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnCompleteClicked)
            assertEquals(
                ItemDetailUiAction.ShowSnackbar(R.string.item_detail_completed_snackbar),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnCompleteConfirmClicked should reopen the task and dismiss the dialog`() = runTest {
        val item = ItemStub.completedTask(id = 1L)
        val reopened = item.copy(completedAt = null)
        every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(reopened))
        coEvery { itemOccurrenceUseCase.complete(any(), any(), any()) } returns Result.success(
            CompletionResult.Uncompleted
        )
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnCompleteClicked)

        vm.onEvent(ItemDetailEvent.OnCompleteConfirmClicked)

        assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        assertEquals(false, vm.uiState.value.isCompleted)
        coVerify(exactly = 1) { itemOccurrenceUseCase.complete(any(), any(), any()) }
    }

    @Test
    fun `when OnCompleteConfirmClicked reopens a task should not emit the completed snackbar`() =
        runTest {
            val item = ItemStub.completedTask(id = 1L)
            val reopened = item.copy(completedAt = null)
            every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(reopened))
            coEvery { itemOccurrenceUseCase.complete(any(), any(), any()) } returns Result.success(
                CompletionResult.Uncompleted
            )
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnCompleteClicked)

            vm.uiAction.test {
                vm.onEvent(ItemDetailEvent.OnCompleteConfirmClicked)
                expectNoEvents()
            }
        }

    @Test
    fun `when OnDialogDismissed after OnCompleteClicked on a completed task should not reopen it`() =
        runTest {
            val item = ItemStub.completedTask(id = 1L)
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnCompleteClicked)

            vm.onEvent(ItemDetailEvent.OnDialogDismissed)

            assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
            assertEquals(true, vm.uiState.value.isCompleted)
            coVerify(exactly = 0) { itemOccurrenceUseCase.complete(any(), any(), any()) }
        }

    @Test
    fun `when completing a recurring task should mark it completed without advancing dueDate`() =
        runTest {
            val item =
                ItemStub.task(id = 1L, recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
            val completed = item.copy(lastCompletedScheduledDate = ItemStub.TODAY)
            every { itemFormUseCase.get(1L) } returnsMany listOf(flowOf(item), flowOf(completed))
            coEvery { itemOccurrenceUseCase.complete(any(), any(), any()) } returns Result.success(
                CompletionResult.Completed
            )
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
            coVerify(exactly = 0) { itemOccurrenceUseCase.complete(any(), any(), any()) }
        }

    @Test
    fun `when OnHistoryClicked should open the history sheet and load the series history`() =
        runTest {
            val item =
                ItemStub.task(id = 1L, recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(emptyList())
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemDetailEvent.OnHistoryClicked)

            assertEquals(ItemDetailDialogState.History, vm.dialogState.value)
            coVerify(exactly = 1) { itemOccurrenceUseCase.getHistory(1L) }
        }

    @Test
    fun `when OnBackRequested with an empty draft should navigate back without saving`() = runTest {
        val vm = viewModel(itemId = null)
        vm.uiState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnBackRequested)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }
        coVerify(exactly = 0) { itemFormUseCase.create(any()) }
    }

    @Test
    fun `when OnBackRequested with a valid title should flush the pending save and navigate back`() =
        runTest {
            coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
            val vm = viewModel(itemId = null)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

            vm.uiAction.test {
                vm.onEvent(ItemDetailEvent.OnBackRequested)
                assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
            }
            coVerify(exactly = 1) { itemFormUseCase.create(match { it.title == "Nova tarefa" }) }
        }

    @Test
    fun `when OnBackRequested is blocked by a blank title the first attempt should only mark titleError`() =
        runTest {
            val vm = viewModel(itemId = null)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnDescriptionChanged("Algo"))

            vm.onEvent(ItemDetailEvent.OnBackRequested)

            assertEquals(true, vm.uiState.value.titleError)
            assertEquals(ItemDetailDialogState.None, vm.dialogState.value)
        }

    @Test
    fun `when OnTitleChanged after a blocked back attempt should clear titleError`() = runTest {
        coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
        val vm = viewModel(itemId = null)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnDescriptionChanged("Algo"))
        vm.onEvent(ItemDetailEvent.OnBackRequested)

        vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))

        assertEquals(false, vm.uiState.value.titleError)
    }

    @Test
    fun `when OnBackRequested is blocked twice in creation mode should open the new-item discard dialog`() =
        runTest {
            val vm = viewModel(itemId = null)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnDescriptionChanged("Algo"))
            vm.onEvent(ItemDetailEvent.OnBackRequested)

            vm.onEvent(ItemDetailEvent.OnBackRequested)

            val dialogState = vm.dialogState.value as ItemDetailDialogState.DiscardConfirm
            assertEquals(R.string.item_detail_discard_new_title, dialogState.titleRes)
            assertEquals(R.string.item_detail_discard_new_message, dialogState.messageRes)
        }

    @Test
    fun `when OnBackRequested is blocked twice in edit mode should open the edit-mode discard dialog`() =
        runTest {
            val item = ItemStub.task(id = 1L)
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnTitleChanged(""))
            vm.onEvent(ItemDetailEvent.OnBackRequested)

            vm.onEvent(ItemDetailEvent.OnBackRequested)

            val dialogState = vm.dialogState.value as ItemDetailDialogState.DiscardConfirm
            assertEquals(R.string.item_detail_discard_edit_title, dialogState.titleRes)
            assertEquals(R.string.item_detail_discard_edit_message, dialogState.messageRes)
        }

    @Test
    fun `when OnDiscardConfirmed in creation mode with nothing auto-saved should just navigate back`() =
        runTest {
            val vm = viewModel(itemId = null)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnDescriptionChanged("Algo"))
            vm.onEvent(ItemDetailEvent.OnBackRequested)
            vm.onEvent(ItemDetailEvent.OnBackRequested)

            vm.uiAction.test {
                vm.onEvent(ItemDetailEvent.OnDiscardConfirmed)
                assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
            }
            coVerify(exactly = 0) { itemFormUseCase.delete(any()) }
        }

    @Test
    fun `when OnDiscardConfirmed in creation mode after an earlier auto-save should delete the orphaned item`() =
        runTest {
            coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
            coEvery { itemFormUseCase.delete(10L) } returns Result.success(Unit)
            val vm = viewModel(itemId = null)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemDetailEvent.OnTitleChanged("Nova tarefa"))
            vm.onEvent(ItemDetailEvent.OnTagToggled(TagStub.tags().first().id))
            vm.onEvent(ItemDetailEvent.OnTitleChanged(""))
            vm.onEvent(ItemDetailEvent.OnBackRequested)
            vm.onEvent(ItemDetailEvent.OnBackRequested)

            vm.uiAction.test {
                vm.onEvent(ItemDetailEvent.OnDiscardConfirmed)
                assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
            }
            coVerify(exactly = 1) { itemFormUseCase.delete(10L) }
        }

    @Test
    fun `when OnDiscardConfirmed in edit mode should never delete the item`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemDetailEvent.OnTitleChanged(""))
        vm.onEvent(ItemDetailEvent.OnBackRequested)
        vm.onEvent(ItemDetailEvent.OnBackRequested)

        vm.uiAction.test {
            vm.onEvent(ItemDetailEvent.OnDiscardConfirmed)
            assertEquals(ItemDetailUiAction.NavigateBack, awaitItem())
        }
        coVerify(exactly = 0) { itemFormUseCase.delete(any()) }
    }

    @Test
    fun `when the ViewModel is recreated with the same SavedStateHandle should restore the draft`() =
        runTest {
            coEvery { itemFormUseCase.create(any()) } returns Result.success(10L)
            val savedStateHandle = SavedStateHandle()
            val firstVm = viewModel(itemId = null, savedStateHandle = savedStateHandle)
            firstVm.uiState.test { awaitItem() }
            firstVm.onEvent(ItemDetailEvent.OnTitleChanged("Rascunho"))

            val secondVm = viewModel(itemId = null, savedStateHandle = savedStateHandle)

            assertEquals("Rascunho", secondVm.uiState.value.title)
        }
}
