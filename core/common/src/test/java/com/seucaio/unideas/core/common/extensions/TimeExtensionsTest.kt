package com.seucaio.unideas.core.common.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class TimeExtensionsTest {

    @Test
    fun `toSecondOfDayInt and toLocalTime round-trip preserves the time`() {
        val time = LocalTime.of(23, 59)

        assertEquals(time, time.toSecondOfDayInt().toLocalTime())
    }

    @Test
    fun `toSecondOfDayInt is timezone-independent`() {
        assertEquals(0, LocalTime.MIDNIGHT.toSecondOfDayInt())
    }
}
