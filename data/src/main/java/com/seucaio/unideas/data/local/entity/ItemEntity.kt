package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence

/**
 * Room entity for the `items` table.
 *
 * Dates are `Long` epoch millis; enums are stored as `TEXT` via `Converters`.
 * `sectionId` FKs to [SectionEntity] with `SET_NULL` on delete — deletion
 * with linked items is blocked at the use-case level before it ever reaches
 * the database, so the FK is a safety net, not the enforcement mechanism.
 */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["sectionId"])],
)
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
