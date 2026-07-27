package com.seucaio.unideas.core.notifications.worker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReminderSchedulerTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 27)

    @Test
    fun `just after midnight returns the 6h slot`() {
        val from = today.atTime(0, 1)

        assertEquals(today.atTime(6, 0), ReminderScheduler.nextCheckSlot(from))
    }

    @Test
    fun `right at a slot boundary returns the next slot, not the same one`() {
        val from = today.atTime(6, 0)

        assertEquals(today.atTime(12, 0), ReminderScheduler.nextCheckSlot(from))
    }

    @Test
    fun `after the last slot of the day rolls over to 0h the next day`() {
        val from = today.atTime(23, 30)

        assertEquals(today.plusDays(1).atTime(0, 0), ReminderScheduler.nextCheckSlot(from))
    }

    @Test
    fun `between two slots returns the closer upcoming one`() {
        val from = today.atTime(13, 45)

        assertEquals(today.atTime(18, 0), ReminderScheduler.nextCheckSlot(from))
    }

    @Test
    fun `check hours are the 4 fixed slots in order`() {
        assertEquals(listOf(0, 6, 12, 18), ReminderScheduler.CHECK_HOURS)
    }
}
