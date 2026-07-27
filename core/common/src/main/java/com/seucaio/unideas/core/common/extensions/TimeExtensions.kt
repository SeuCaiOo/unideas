package com.seucaio.unideas.core.common.extensions

import java.time.LocalTime

/** Converts seconds-of-day (as persisted in the database) to a [LocalTime]. */
fun Int.toLocalTime(): LocalTime = LocalTime.ofSecondOfDay(this.toLong())

/** Converts a [LocalTime] to seconds-of-day (for writing to the database). */
fun LocalTime.toSecondOfDayInt(): Int = toSecondOfDay()
