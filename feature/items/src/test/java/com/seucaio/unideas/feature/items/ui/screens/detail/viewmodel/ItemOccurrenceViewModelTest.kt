package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.CompletionStatus
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.stub.ItemStub
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ItemOccurrenceViewModelTest {

    @MockK
    private lateinit var itemFormUseCase: ItemFormUseCase

    @MockK
    private lateinit var itemOccurrenceUseCase: ItemOccurrenceUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long? = null) =
        ItemOccurrenceViewModel(
            itemId = itemId,
            itemFormUseCase = itemFormUseCase,
            itemOccurrenceUseCase = itemOccurrenceUseCase,
        )

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

            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            assertEquals(ItemOccurrenceDialogState.None, vm.dialogState.value)
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

            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            assertEquals(ItemOccurrenceDialogState.ReopenConfirm, vm.dialogState.value)
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
            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)
            awaitItem() // ItemPersisted
            assertEquals(
                ItemOccurrenceUiAction.ShowSnackbar(R.string.item_detail_completed_snackbar),
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
        vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

        vm.onEvent(ItemOccurrenceEvent.OnCompleteConfirmClicked)

        assertEquals(ItemOccurrenceDialogState.None, vm.dialogState.value)
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
            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            vm.uiAction.test {
                vm.onEvent(ItemOccurrenceEvent.OnCompleteConfirmClicked)
                awaitItem() // ItemPersisted
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
            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            vm.onEvent(ItemOccurrenceEvent.OnDialogDismissed)

            assertEquals(ItemOccurrenceDialogState.None, vm.dialogState.value)
            assertEquals(true, vm.uiState.value.isCompleted)
            coVerify(exactly = 0) { itemOccurrenceUseCase.complete(any(), any(), any()) }
        }

    @Test
    fun `when OnCompleteClicked on a pending recurring task should open the complete confirmation dialog`() =
        runTest {
            val item = ItemStub.task(id = 1L, recurrence = Recurrence.Weekly, dueDate = LocalDate.now())
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            assertEquals(ItemOccurrenceDialogState.CompleteConfirm(isLate = false), vm.dialogState.value)
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
            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            vm.onEvent(ItemOccurrenceEvent.OnCompleteWithNoteConfirmClicked(note = null))

            assertEquals(ItemOccurrenceDialogState.None, vm.dialogState.value)
            assertEquals(true, vm.uiState.value.isCompleted)
            coVerify(exactly = 1) { itemOccurrenceUseCase.complete(item, any(), null) }
        }

    @Test
    fun `when OnCompleteClicked on a pending late recurring task should require a note`() = runTest {
        val item = ItemStub.task(
            id = 1L,
            recurrence = Recurrence.Weekly,
            dueDate = LocalDate.now().minusDays(1),
        )
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

        assertEquals(ItemOccurrenceDialogState.CompleteConfirm(isLate = true), vm.dialogState.value)
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

            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            assertEquals(ItemOccurrenceDialogState.ReopenConfirm, vm.dialogState.value)
            coVerify(exactly = 0) { itemOccurrenceUseCase.complete(any(), any(), any()) }
        }

    @Test
    fun `when OnIgnoreClicked should open the ignore confirmation dialog`() = runTest {
        val item = ItemStub.task(
            id = 1L,
            recurrence = Recurrence.Weekly,
            dueDate = LocalDate.now().minusDays(1),
        )
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemOccurrenceEvent.OnIgnoreClicked)

        assertEquals(ItemOccurrenceDialogState.IgnoreConfirm, vm.dialogState.value)
        coVerify(exactly = 0) { itemOccurrenceUseCase.ignore(any(), any(), any()) }
    }

    @Test
    fun `when OnIgnoreConfirmClicked should ignore the occurrence and advance dueDate`() = runTest {
        val item = ItemStub.task(
            id = 1L,
            recurrence = Recurrence.Weekly,
            dueDate = LocalDate.now().minusDays(1),
        )
        val advanced = item.copy(dueDate = LocalDate.now().plusDays(6))
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        coEvery { itemOccurrenceUseCase.ignore(any(), any(), any()) } returns Result.success(advanced)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemOccurrenceEvent.OnIgnoreClicked)

        vm.onEvent(ItemOccurrenceEvent.OnIgnoreConfirmClicked(note = "Estava viajando"))

        assertEquals(ItemOccurrenceDialogState.None, vm.dialogState.value)
        assertEquals(advanced.dueDate, vm.uiState.value.dueDate)
        coVerify(exactly = 1) { itemOccurrenceUseCase.ignore(item, "Estava viajando", any()) }
    }

    @Test
    fun `when OnExtendDeadlineClicked should open the date picker with the current dueDate`() =
        runTest {
            val dueDate = LocalDate.now().minusDays(2)
            val item = ItemStub.task(id = 1L, dueDate = dueDate)
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemOccurrenceEvent.OnExtendDeadlineClicked)

            assertEquals(ItemOccurrenceDialogState.ExtendDeadlineConfirm(dueDate), vm.dialogState.value)
            coVerify(exactly = 0) { itemOccurrenceUseCase.extendDueDate(any(), any(), any()) }
        }

    @Test
    fun `when OnExtendDeadlineConfirmClicked should extend the dueDate`() = runTest {
        val dueDate = LocalDate.now().minusDays(2)
        val newDueDate = LocalDate.now().plusDays(3)
        val item = ItemStub.task(id = 1L, dueDate = dueDate)
        val extended = item.copy(dueDate = newDueDate)
        every { itemFormUseCase.get(1L) } returns flowOf(item)
        coEvery { itemOccurrenceUseCase.extendDueDate(any(), any(), any()) } returns Result.success(extended)
        val vm = viewModel(itemId = 1L)
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemOccurrenceEvent.OnExtendDeadlineClicked)

        vm.onEvent(ItemOccurrenceEvent.OnExtendDeadlineConfirmClicked(newDueDate))

        assertEquals(ItemOccurrenceDialogState.None, vm.dialogState.value)
        assertEquals(newDueDate, vm.uiState.value.dueDate)
        coVerify(exactly = 1) { itemOccurrenceUseCase.extendDueDate(item, newDueDate, any()) }
    }

    @Test
    fun `when OnItemUpdatedExternally changes recurrence a subsequent complete should use the updated item`() =
        runTest {
            val item = ItemStub.task(id = 1L, recurrence = Recurrence.None, dueDate = null)
            val recurringUpdate = item.copy(recurrence = Recurrence.Weekly, dueDate = LocalDate.now())
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }
            vm.onEvent(ItemOccurrenceEvent.OnItemUpdatedExternally(recurringUpdate))

            vm.onEvent(ItemOccurrenceEvent.OnCompleteClicked)

            assertEquals(ItemOccurrenceDialogState.CompleteConfirm(isLate = false), vm.dialogState.value)
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

            vm.onEvent(ItemOccurrenceEvent.OnHistoryClicked)

            assertEquals(ItemOccurrenceDialogState.History, vm.dialogState.value)
            coVerify(exactly = 1) { itemOccurrenceUseCase.getHistory(1L) }
        }

    @Test
    fun `when OnHistoryClicked should expose each entry's status and note in historyState`() =
        runTest {
            val item =
                ItemStub.task(id = 1L, recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
            val onTime = ItemCompletionHistory(
                id = 1L,
                itemId = 1L,
                scheduledDate = LocalDate.of(2026, 7, 1),
                completedAt = LocalDate.of(2026, 7, 1).atTime(10, 0),
            )
            val late = ItemCompletionHistory(
                id = 2L,
                itemId = 1L,
                scheduledDate = LocalDate.of(2026, 7, 8),
                completedAt = LocalDate.of(2026, 7, 9).atTime(10, 0),
                note = "Sem tempo no dia",
            )
            val missed = ItemCompletionHistory(
                id = 3L,
                itemId = 1L,
                scheduledDate = LocalDate.of(2026, 7, 15),
                completedAt = null,
                note = "Não deu essa semana",
            )
            every { itemFormUseCase.get(1L) } returns flowOf(item)
            every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime, late, missed))
            val vm = viewModel(itemId = 1L)
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemOccurrenceEvent.OnHistoryClicked)

            assertEquals(listOf(onTime, late, missed), vm.historyState.value)
            assertEquals(CompletionStatus.ON_TIME, vm.historyState.value[0].status)
            assertEquals(CompletionStatus.LATE, vm.historyState.value[1].status)
            assertEquals("Sem tempo no dia", vm.historyState.value[1].note)
            assertEquals(CompletionStatus.MISSED, vm.historyState.value[2].status)
            assertEquals("Não deu essa semana", vm.historyState.value[2].note)
        }
}
