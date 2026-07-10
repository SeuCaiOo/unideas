package com.seucaio.unideas.data.local.converter

import androidx.room.TypeConverter
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence

/**
 * Room [TypeConverter]s for enum/sealed columns (stored as a single string).
 *
 * Dates are persisted as raw `Long` epoch millis — no converter on purpose
 * (see `docs/ARCHITECTURE.md`); the Entity ↔ Domain mappers do the conversion.
 */
class Converters {

    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = ItemType.valueOf(value)

    @TypeConverter
    fun fromRecurrence(value: Recurrence): String = when (value) {
        Recurrence.None -> NONE
        Recurrence.Daily -> DAILY
        Recurrence.Weekly -> WEEKLY
        Recurrence.Monthly -> MONTHLY
        is Recurrence.EveryNDays -> "$EVERY_N_DAYS_PREFIX${value.days}"
    }

    @TypeConverter
    fun toRecurrence(value: String): Recurrence = when {
        value == NONE -> Recurrence.None
        value == DAILY -> Recurrence.Daily
        value == WEEKLY -> Recurrence.Weekly
        value == MONTHLY -> Recurrence.Monthly
        value.startsWith(EVERY_N_DAYS_PREFIX) ->
            Recurrence.EveryNDays(value.removePrefix(EVERY_N_DAYS_PREFIX).toInt())
        else -> error("Unknown recurrence: $value")
    }

    private companion object {
        const val NONE = "NONE"
        const val DAILY = "DAILY"
        const val WEEKLY = "WEEKLY"
        const val MONTHLY = "MONTHLY"
        const val EVERY_N_DAYS_PREFIX = "EVERY_N_DAYS:"
    }
}
