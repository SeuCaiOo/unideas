package com.seucaio.unideas.core.common.extensions

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

class DateTimeExtensionsTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        // Pin a non-UTC zone (UTC-3, no DST) so the system-default conversions
        // are deterministic regardless of the machine running the tests.
        TimeZone.setDefault(TimeZone.getTimeZone(SAO_PAULO))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `toFormattedDateString formats LocalDateTime date portion`() {
        val dateTime = LocalDateTime.of(2026, 12, 31, 23, 59, 58)

        assertEquals("31/12/2026", dateTime.toFormattedDateString())
    }

    @Test
    fun `toEpochMilli uses system default zone`() {
        val dateTime = LocalDateTime.of(2026, 7, 9, 10, 30)

        val expected = dateTime.atZone(ZoneId.of(SAO_PAULO)).toInstant().toEpochMilli()

        assertEquals(expected, dateTime.toEpochMilli())
    }

    @Test
    fun `toEpochMilli and toLocalDateTime round-trip`() {
        val dateTime = LocalDateTime.of(2026, 2, 28, 23, 59, 59)

        assertEquals(dateTime, dateTime.toEpochMilli().toLocalDateTime())
    }

    @Test
    fun `toFormattedDateTimeString formats LocalDateTime as MM-dd-yyyy HH-mm pattern`() {
        assertEquals("12/31/2026 23:59", LocalDateTime.of(2026, 12, 31, 23, 59, 58).toFormattedDateTimeString())
    }

    private companion object {
        const val SAO_PAULO = "America/Sao_Paulo"
    }
}
