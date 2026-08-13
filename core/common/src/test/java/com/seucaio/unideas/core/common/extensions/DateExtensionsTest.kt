package com.seucaio.unideas.core.common.extensions

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.TimeZone

class DateExtensionsTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        // Pin a non-UTC zone (UTC-3, no DST) so the system-default vs UTC
        // distinction is deterministic regardless of the machine running the tests.
        TimeZone.setDefault(TimeZone.getTimeZone(SAO_PAULO))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `toLocalDate converts epoch millis using system default zone`() {
        val millis = LocalDate.of(2026, 7, 9)
            .atStartOfDay(ZoneId.of(SAO_PAULO))
            .toInstant()
            .toEpochMilli()

        assertEquals(LocalDate.of(2026, 7, 9), millis.toLocalDate())
    }

    @Test
    fun `toEpochMilli converts start of day in system default zone`() {
        val date = LocalDate.of(2026, 7, 9)

        val expected = date.atStartOfDay(ZoneId.of(SAO_PAULO)).toInstant().toEpochMilli()

        assertEquals(expected, date.toEpochMilli())
    }

    @Test
    fun `toEpochMilli and toLocalDate round-trip preserves the date`() {
        val date = LocalDate.of(2026, 2, 28)

        assertEquals(date, date.toEpochMilli().toLocalDate())
    }

    @Test
    fun `toLocalDateUtc converts epoch millis using UTC`() {
        val utcMidnight = LocalDate.of(2026, 7, 9)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        assertEquals(LocalDate.of(2026, 7, 9), utcMidnight.toLocalDateUtc())
    }

    @Test
    fun `toLocalDate and toLocalDateUtc differ for UTC midnight millis in a negative-offset zone`() {
        // Material3 DatePicker returns UTC midnight. In UTC-3 that instant is
        // still 21:00 of the PREVIOUS day, so the system-default conversion
        // would land on the wrong date.
        val pickerMillis = LocalDate.of(2026, 7, 9)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        assertEquals(LocalDate.of(2026, 7, 9), pickerMillis.toLocalDateUtc())
        assertEquals(LocalDate.of(2026, 7, 8), pickerMillis.toLocalDate())
    }

    @Test
    fun `toFormattedDateString formats LocalDate as dd-MM-yyyy pattern`() {
        assertEquals("09/07/2026", LocalDate.of(2026, 7, 9).toFormattedDateString())
        assertEquals("01/01/2001", LocalDate.of(2001, 1, 1).toFormattedDateString())
    }

    @Test
    fun `nextOrSameDayOfMonth returns this month when the day hasn't passed yet`() {
        val today = LocalDate.of(2026, 8, 9)

        assertEquals(LocalDate.of(2026, 8, 20), today.nextOrSameDayOfMonth(20))
    }

    @Test
    fun `nextOrSameDayOfMonth returns the same date when today is the chosen day`() {
        val today = LocalDate.of(2026, 8, 9)

        assertEquals(today, today.nextOrSameDayOfMonth(9))
    }

    @Test
    fun `nextOrSameDayOfMonth rolls over to next month when the day already passed`() {
        val today = LocalDate.of(2026, 8, 9)

        assertEquals(LocalDate.of(2026, 9, 3), today.nextOrSameDayOfMonth(3))
    }

    @Test
    fun `nextOrSameDayOfMonth clamps to the shortest month instead of rolling over`() {
        val today = LocalDate.of(2026, 2, 9)

        assertEquals(LocalDate.of(2026, 2, 28), today.nextOrSameDayOfMonth(30))
    }

    @Test
    fun `nextOrSame returns the same date when today already is that weekday`() {
        val today = LocalDate.of(2026, 8, 9)

        assertEquals(today, today.nextOrSame(today.dayOfWeek))
    }

    @Test
    fun `nextOrSame returns the next matching weekday within 7 days`() {
        val today = LocalDate.of(2026, 8, 9)
        val target = DayOfWeek.entries.first { it != today.dayOfWeek }

        val result = today.nextOrSame(target)

        assertTrue(!result.isBefore(today))
        assertEquals(target, result.dayOfWeek)
        assertTrue(ChronoUnit.DAYS.between(today, result) < 7)
    }

    @Test
    fun `orToday returns this date when not null`() {
        val date = LocalDate.of(2026, 8, 9)

        assertEquals(date, date.orToday())
    }

    @Test
    fun `orToday returns today when null`() {
        val nullDate: LocalDate? = null

        assertEquals(LocalDate.now(), nullDate.orToday())
    }

    private companion object {
        const val SAO_PAULO = "America/Sao_Paulo"
    }
}
