package com.seucaio.unideas.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test

class ReminderWarningTest {

    @Test
    fun `DaysBefore accepts a positive day count`() {
        val warning = ReminderWarning.DaysBefore(3)

        assert(warning.days == 3)
    }

    @Test
    fun `DaysBefore rejects zero or negative days`() {
        assertThrows(IllegalArgumentException::class.java) { ReminderWarning.DaysBefore(0) }
        assertThrows(IllegalArgumentException::class.java) { ReminderWarning.DaysBefore(-1) }
    }
}
