package com.seucaio.unideas.domain.model

import com.seucaio.unideas.domain.stub.ItemStub
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ReminderTierTest {

    private val dueDate: LocalDate = ItemStub.TODAY.plusDays(3)

    private fun check(date: LocalDate, hour: Int) = date.atTime(hour, 0)

    @Test
    fun `no dueDate is NOT_YET`() {
        val item = ItemStub.task(dueDate = null)

        val tier = ReminderTier.of(item, check(ItemStub.TODAY, 0), check(ItemStub.TODAY, 6))

        assertEquals(ReminderTier.NOT_YET, tier)
    }

    @Test
    fun `no warning configured and far from due date is NOT_YET`() {
        val item = ItemStub.task(dueDate = dueDate, reminderWarning = ReminderWarning.None)

        val tier = ReminderTier.of(item, check(ItemStub.TODAY, 0), check(ItemStub.TODAY, 6))

        assertEquals(ReminderTier.NOT_YET, tier)
    }

    @Test
    fun `before the warning window is NOT_YET`() {
        val item = ItemStub.task(dueDate = dueDate, reminderWarning = ReminderWarning.DaysBefore(2))
        val today = dueDate.minusDays(3)

        val tier = ReminderTier.of(item, check(today, 0), check(today, 6))

        assertEquals(ReminderTier.NOT_YET, tier)
    }

    @Test
    fun `at the start of the warning window is NORMAL`() {
        val item = ItemStub.task(dueDate = dueDate, reminderWarning = ReminderWarning.DaysBefore(2))
        val today = dueDate.minusDays(2)

        val tier = ReminderTier.of(item, check(today, 0), check(today, 6))

        assertEquals(ReminderTier.NORMAL, tier)
    }

    @Test
    fun `on the due date with no dueTime is NORMAL before the last check`() {
        val item = ItemStub.task(dueDate = dueDate, reminderWarning = ReminderWarning.DaysBefore(2))

        val tier = ReminderTier.of(item, check(dueDate, 0), check(dueDate, 6))

        assertEquals(ReminderTier.NORMAL, tier)
    }

    @Test
    fun `on the due date with no dueTime is URGENT on the last check of the day`() {
        val item = ItemStub.task(dueDate = dueDate)

        val tier = ReminderTier.of(item, check(dueDate, 18), check(dueDate.plusDays(1), 0))

        assertEquals(ReminderTier.URGENT, tier)
    }

    @Test
    fun `dueTime before the next check is promoted to URGENT early (last chance rule)`() {
        // Due at 12h: the 6h check is the last chance, since the 12h check would already be late.
        val item = ItemStub.task(dueDate = dueDate, dueTime = LocalTime.of(12, 0))

        val tier = ReminderTier.of(item, check(dueDate, 6), check(dueDate, 12))

        assertEquals(ReminderTier.URGENT, tier)
    }

    @Test
    fun `dueTime after the next check is not urgent yet`() {
        // Due at 18h: the 12h check still has the 18h check ahead of it.
        val item = ItemStub.task(dueDate = dueDate, dueTime = LocalTime.of(18, 0))

        val tier = ReminderTier.of(item, check(dueDate, 6), check(dueDate, 12))

        assertEquals(ReminderTier.NOT_YET, tier)
    }

    @Test
    fun `overdue is URGENT`() {
        val item = ItemStub.task(dueDate = ItemStub.TODAY.minusDays(1))

        val tier = ReminderTier.of(item, check(ItemStub.TODAY, 0), check(ItemStub.TODAY, 6))

        assertEquals(ReminderTier.URGENT, tier)
    }
}
