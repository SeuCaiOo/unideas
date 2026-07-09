package com.seucaio.unideas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class UrgencyLevelTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 1)
    private val dueSoonDays = 3

    @Test
    fun `null dueDate is NORMAL`() {
        assertEquals(UrgencyLevel.NORMAL, UrgencyLevel.of(null, today, dueSoonDays))
    }

    @Test
    fun `dueDate before today is OVERDUE`() {
        assertEquals(UrgencyLevel.OVERDUE, UrgencyLevel.of(today.minusDays(1), today, dueSoonDays))
    }

    @Test
    fun `dueDate today is DUE_SOON`() {
        assertEquals(UrgencyLevel.DUE_SOON, UrgencyLevel.of(today, today, dueSoonDays))
    }

    @Test
    fun `dueDate at threshold is DUE_SOON`() {
        assertEquals(
            UrgencyLevel.DUE_SOON,
            UrgencyLevel.of(today.plusDays(dueSoonDays.toLong()), today, dueSoonDays),
        )
    }

    @Test
    fun `dueDate past threshold is NORMAL`() {
        assertEquals(
            UrgencyLevel.NORMAL,
            UrgencyLevel.of(today.plusDays(dueSoonDays + 1L), today, dueSoonDays),
        )
    }
}
