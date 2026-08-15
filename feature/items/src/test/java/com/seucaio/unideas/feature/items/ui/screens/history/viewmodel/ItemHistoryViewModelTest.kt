package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.CompletionStatus
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
import io.mockk.MockKAnnotations
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
}
