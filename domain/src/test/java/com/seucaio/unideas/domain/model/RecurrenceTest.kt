package com.seucaio.unideas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    private val from: LocalDate = LocalDate.of(2026, 7, 1)

    @Test
    fun `None has no next due date`() {
        assertNull(Recurrence.None.nextDueDate(from))
    }

    @Test
    fun `Daily advances one day`() {
        assertEquals(LocalDate.of(2026, 7, 2), Recurrence.Daily.nextDueDate(from))
    }

    @Test
    fun `Weekly advances seven days`() {
        assertEquals(LocalDate.of(2026, 7, 8), Recurrence.Weekly.nextDueDate(from))
    }

    @Test
    fun `Monthly advances one month`() {
        assertEquals(LocalDate.of(2026, 8, 1), Recurrence.Monthly.nextDueDate(from))
    }

    @Test
    fun `Monthly clamps to last day of shorter month`() {
        assertEquals(
            LocalDate.of(2026, 2, 28),
            Recurrence.Monthly.nextDueDate(LocalDate.of(2026, 1, 31)),
        )
    }

    @Test
    fun `EveryNDays advances the given number of days`() {
        assertEquals(LocalDate.of(2026, 7, 4), Recurrence.EveryNDays(3).nextDueDate(from))
    }

    @Test
    fun `EveryNDays biweekly constant advances 15 days`() {
        assertEquals(
            LocalDate.of(2026, 7, 16),
            Recurrence.EveryNDays(Recurrence.EveryNDays.BIWEEKLY_DAYS).nextDueDate(from),
        )
    }

    @Test
    fun `EveryNDays every-other-day constant advances 2 days`() {
        assertEquals(
            LocalDate.of(2026, 7, 3),
            Recurrence.EveryNDays(Recurrence.EveryNDays.EVERY_OTHER_DAY_DAYS).nextDueDate(from),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `EveryNDays rejects non-positive days`() {
        Recurrence.EveryNDays(0)
    }
}
