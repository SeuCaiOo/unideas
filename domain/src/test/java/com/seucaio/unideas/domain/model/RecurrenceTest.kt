package com.seucaio.unideas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    private val from: LocalDate = LocalDate.of(2026, 7, 1)

    @Test
    fun `NONE has no next due date`() {
        assertNull(Recurrence.NONE.nextDueDate(from))
    }

    @Test
    fun `DAILY advances one day`() {
        assertEquals(LocalDate.of(2026, 7, 2), Recurrence.DAILY.nextDueDate(from))
    }

    @Test
    fun `WEEKLY advances seven days`() {
        assertEquals(LocalDate.of(2026, 7, 8), Recurrence.WEEKLY.nextDueDate(from))
    }

    @Test
    fun `MONTHLY advances one month`() {
        assertEquals(LocalDate.of(2026, 8, 1), Recurrence.MONTHLY.nextDueDate(from))
    }

    @Test
    fun `MONTHLY clamps to last day of shorter month`() {
        assertEquals(
            LocalDate.of(2026, 2, 28),
            Recurrence.MONTHLY.nextDueDate(LocalDate.of(2026, 1, 31)),
        )
    }
}
