package com.seucaio.unideas.core.common.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

/** Formats this [LocalDate] as `dd/MM/yyyy`. */
fun LocalDate.toFormattedDateString(): String = format(dateFormatter)

/** Formats the date portion of this [LocalDateTime] as `dd/MM/yyyy`. */
fun LocalDateTime.toFormattedDateString(): String = toLocalDate().toFormattedDateString()
