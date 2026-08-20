package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.CompletionStatus
import com.seucaio.unideas.domain.model.ItemCompletionHistory
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ItemHistoryViewModelTest {

    @MockK
    private lateinit var itemOccurrenceUseCase: ItemOccurrenceUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    private val onTime = ItemCompletionHistory(
        id = 1L,
        itemId = 1L,
        scheduledDate = LocalDate.of(2026, 7, 15),
        completedAt = LocalDateTime.of(2026, 7, 15, 9, 0),
    )
    private val late = ItemCompletionHistory(
        id = 2L,
        itemId = 1L,
        scheduledDate = LocalDate.of(2026, 7, 8),
        completedAt = LocalDateTime.of(2026, 7, 9, 20, 0),
        note = "Sem tempo no dia",
    )
    private val missed = ItemCompletionHistory(
        id = 3L,
        itemId = 1L,
        scheduledDate = LocalDate.of(2026, 7, 1),
        completedAt = null,
        note = "Não deu essa semana",
    )
    private val extended = ItemCompletionHistory(
        id = 4L,
        itemId = 1L,
        scheduledDate = LocalDate.of(2026, 6, 24),
        completedAt = LocalDateTime.of(2026, 6, 24, 10, 0),
        originalScheduledDate = LocalDate.of(2026, 6, 17),
        extensionCount = 2,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long = 1L) =
        ItemHistoryViewModel(itemId = itemId, itemOccurrenceUseCase = itemOccurrenceUseCase)

    @Test
    fun `when history loads should expose status, note and extension fields for every entry`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime, late, missed, extended))
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(onTime, late, missed, extended), state.history)
            assertEquals(CompletionStatus.ON_TIME, state.history[0].status)
            assertEquals(CompletionStatus.LATE, state.history[1].status)
            assertEquals("Sem tempo no dia", state.history[1].note)
            assertEquals(CompletionStatus.MISSED, state.history[2].status)
            assertEquals(2, state.history[3].extensionCount)
            assertEquals(LocalDate.of(2026, 6, 17), state.history[3].originalScheduledDate)
        }
    }

    @Test
    fun `when history loads should compute the on-time rate and counts by status`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime, late, missed, extended))
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(4, state.history.size)
            assertEquals(2, state.onTimeCount)
            assertEquals(1, state.lateCount)
            assertEquals(1, state.missedCount)
            assertEquals(50, state.onTimeRatePercent)
        }
    }

    @Test
    fun `when the most recent entries are on time should compute the current streak`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime, extended, late, missed))
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.currentStreak)
        }
    }

    @Test
    fun `when the most recent entry is not on time should have a zero streak`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(late, onTime))
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.currentStreak)
        }
    }

    @Test
    fun `when OnFilterSelected should keep the full history but expose only matching entries`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime, late, missed))
        val vm = viewModel()

        vm.onEvent(ItemHistoryEvent.OnFilterSelected(HistoryFilter.LATE))

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(HistoryFilter.LATE, state.activeFilter)
            assertEquals(3, state.history.size)
            assertEquals(listOf(late), state.filteredHistory)
        }
    }

    @Test
    fun `when filtering by WITH_NOTE should only expose entries with a non-blank note`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime, late, missed))
        val vm = viewModel()

        vm.onEvent(ItemHistoryEvent.OnFilterSelected(HistoryFilter.WITH_NOTE))

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(late, missed), state.filteredHistory)
        }
    }

    @Test
    fun `when OnAddEntryClicked should open the AddEditEntry dialog with no existing entry`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(emptyList())
        val vm = viewModel()

        vm.onEvent(ItemHistoryEvent.OnAddEntryClicked)

        assertEquals(ItemHistoryDialogState.AddEditEntry(null), vm.dialogState.value)
    }

    @Test
    fun `when OnEditEntryClicked should open the AddEditEntry dialog with that entry`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime))
        val vm = viewModel()

        vm.onEvent(ItemHistoryEvent.OnEditEntryClicked(onTime))

        assertEquals(ItemHistoryDialogState.AddEditEntry(onTime), vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteEntryClicked should open the DeleteConfirm dialog for that entry`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime))
        val vm = viewModel()

        vm.onEvent(ItemHistoryEvent.OnDeleteEntryClicked(onTime))

        assertEquals(ItemHistoryDialogState.DeleteConfirm(onTime), vm.dialogState.value)
    }

    @Test
    fun `when OnDialogDismissed should close the dialog`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(emptyList())
        val vm = viewModel()
        vm.onEvent(ItemHistoryEvent.OnAddEntryClicked)

        vm.onEvent(ItemHistoryEvent.OnDialogDismissed)

        assertEquals(ItemHistoryDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnEntrySubmitted with no existing entry should save a new record and close the dialog`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(emptyList())
        val vm = viewModel()
        vm.onEvent(ItemHistoryEvent.OnAddEntryClicked)
        val scheduledDate = LocalDate.of(2026, 7, 20)
        val completedAt = LocalDateTime.of(2026, 7, 20, 12, 0)
        val expected = ItemCompletionHistory(
            id = 0L,
            itemId = 1L,
            scheduledDate = scheduledDate,
            completedAt = completedAt,
            note = "Feito",
        )
        coEvery { itemOccurrenceUseCase.saveHistoryEntry(expected) } returns Result.success(Unit)

        vm.onEvent(ItemHistoryEvent.OnEntrySubmitted(scheduledDate, completedAt, "Feito"))

        coVerify(exactly = 1) { itemOccurrenceUseCase.saveHistoryEntry(expected) }
        assertEquals(ItemHistoryDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnEntrySubmitted while editing should save with the existing entry's id`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime))
        val vm = viewModel()
        vm.onEvent(ItemHistoryEvent.OnEditEntryClicked(onTime))
        val expected = onTime.copy(note = "Ajustado")
        coEvery { itemOccurrenceUseCase.saveHistoryEntry(expected) } returns Result.success(Unit)

        vm.onEvent(ItemHistoryEvent.OnEntrySubmitted(onTime.scheduledDate, onTime.completedAt, "Ajustado"))

        coVerify(exactly = 1) { itemOccurrenceUseCase.saveHistoryEntry(expected) }
        assertEquals(ItemHistoryDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnEntrySubmitted fails with a validation error should emit ShowSnackbar and keep the dialog open`() =
        runTest {
            every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(emptyList())
            val vm = viewModel()
            vm.onEvent(ItemHistoryEvent.OnAddEntryClicked)
            coEvery { itemOccurrenceUseCase.saveHistoryEntry(any()) } returns
                Result.failure(IllegalArgumentException("boom"))

            vm.uiAction.test {
                vm.onEvent(ItemHistoryEvent.OnEntrySubmitted(LocalDate.of(2026, 7, 20), null, null))
                assertEquals(ItemHistoryUiAction.ShowSnackbar(R.string.item_history_entry_invalid), awaitItem())
            }
            assertEquals(ItemHistoryDialogState.AddEditEntry(null), vm.dialogState.value)
        }

    @Test
    fun `when OnEntrySubmitted fails unexpectedly should emit ShowError`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(emptyList())
        val vm = viewModel()
        vm.onEvent(ItemHistoryEvent.OnAddEntryClicked)
        coEvery { itemOccurrenceUseCase.saveHistoryEntry(any()) } returns Result.failure(IllegalStateException("boom"))

        vm.uiAction.test {
            vm.onEvent(ItemHistoryEvent.OnEntrySubmitted(LocalDate.of(2026, 7, 20), null, null))
            assertEquals(ItemHistoryUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteConfirmClicked succeeds should delete the entry and close the dialog`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime))
        val vm = viewModel()
        vm.onEvent(ItemHistoryEvent.OnDeleteEntryClicked(onTime))
        coEvery { itemOccurrenceUseCase.deleteHistoryEntry(onTime.id) } returns Result.success(Unit)

        vm.onEvent(ItemHistoryEvent.OnDeleteConfirmClicked)

        coVerify(exactly = 1) { itemOccurrenceUseCase.deleteHistoryEntry(onTime.id) }
        assertEquals(ItemHistoryDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteConfirmClicked fails should emit ShowError and keep the dialog open`() = runTest {
        every { itemOccurrenceUseCase.getHistory(1L) } returns flowOf(listOf(onTime))
        val vm = viewModel()
        vm.onEvent(ItemHistoryEvent.OnDeleteEntryClicked(onTime))
        coEvery { itemOccurrenceUseCase.deleteHistoryEntry(onTime.id) } returns
            Result.failure(IllegalStateException("boom"))

        vm.uiAction.test {
            vm.onEvent(ItemHistoryEvent.OnDeleteConfirmClicked)
            assertEquals(ItemHistoryUiAction.ShowError("boom"), awaitItem())
        }
        assertEquals(ItemHistoryDialogState.DeleteConfirm(onTime), vm.dialogState.value)
    }
}
