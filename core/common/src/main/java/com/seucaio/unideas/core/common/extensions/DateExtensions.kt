package com.seucaio.unideas.core.common.extensions

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private const val DATE_PATTERN = "dd/MM/yyyy"

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

/**
 * Converts epoch millis (as persisted in the database) to a [LocalDate]
 * using the system default time zone.
 */
fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

/**
 * Converts a [LocalDate] to epoch millis at the start of day in the system
 * default time zone (for writing to the database).
 */
fun LocalDate.toEpochMilli(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Converts epoch millis to a [LocalDate] using UTC.
 *
 * Material3 DatePicker/DateRangePicker return UTC midnight millis, so they
 * must be converted with UTC — not with the system default zone used for
 * persisted values.
 */
fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * Converts this [LocalDate] to UTC midnight epoch millis — the inverse of [toLocalDateUtc],
 * for pre-selecting a Material3 DatePicker/DateRangePicker with an existing date.
 */
fun LocalDate.toEpochMilliUtc(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** Formats this [LocalDate] as `dd/MM/yyyy`. */
fun LocalDate.toFormattedDateString(): String = format(dateFormatter)

/**
 * Next date (from this one) landing on [day] of the month, clamped to the shortest month in
 * between — e.g. picking 30 while already past it in February resolves to February's 28th/29th,
 * not March's.
 */
fun LocalDate.nextOrSameDayOfMonth(day: Int): LocalDate {
    val thisMonth = withDayOfMonth(minOf(day, lengthOfMonth()))
    if (!thisMonth.isBefore(this)) return thisMonth

    val nextMonth = plusMonths(1)
    return nextMonth.withDayOfMonth(minOf(day, nextMonth.lengthOfMonth()))
}

/** Next date (from this one) landing on [dayOfWeek], or this date itself if it already is. */
fun LocalDate.nextOrSame(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.nextOrSame(dayOfWeek))

/** This date, or today if `null`. */
fun LocalDate?.orToday(): LocalDate = this ?: LocalDate.now()
