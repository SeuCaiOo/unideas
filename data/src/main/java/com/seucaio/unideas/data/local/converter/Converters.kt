package com.seucaio.unideas.data.local.converter

import androidx.room.TypeConverter
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence

/**
 * Room [TypeConverter]s for enum columns (stored as their `name` string).
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
    fun fromRecurrence(value: Recurrence): String = value.name

    @TypeConverter
    fun toRecurrence(value: String): Recurrence = Recurrence.valueOf(value)
}
