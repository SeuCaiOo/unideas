package com.seucaio.unideas.core.common.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm"

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)

/**
 * Converts epoch millis (as persisted in the database) to a [LocalDateTime]
 * using the system default time zone.
 */
fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

/**
 * Converts a [LocalDateTime] to epoch millis in the system default time zone
 * (for writing to the database).
 */
fun LocalDateTime.toEpochMilli(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Formats the date portion of this [LocalDateTime] as `dd/MM/yyyy`. */
fun LocalDateTime.toFormattedDateString(): String = toLocalDate().toFormattedDateString()

/** Formats this [LocalDateTime] as `MM/dd/yyyy HH:mm`. */
fun LocalDateTime.toFormattedDateTimeString(): String = format(dateTimeFormatter)
