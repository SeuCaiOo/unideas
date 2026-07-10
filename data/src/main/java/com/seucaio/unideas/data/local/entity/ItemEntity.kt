package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence

/**
 * Room entity for the `items` table.
 *
 * Dates are `Long` epoch millis; enums are stored as `TEXT` via `Converters`.
 * `sectionId` is a plain nullable column for now — the FK to `sections` lands
 * with the Section persistence sub-issue.
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: ItemType,
    val title: String,
    val description: String? = null,
    val sectionId: Long? = null,
    val dueDate: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val completedAt: Long? = null,
    val createdAt: Long,
)
