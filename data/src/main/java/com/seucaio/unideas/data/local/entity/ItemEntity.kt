package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning

/**
 * Room entity for the `items` table.
 *
 * Dates are `Long` epoch millis; [dueTime] is seconds-of-day (converted in the mapper, same as
 * dates — not a `Converters` type since it isn't an enum/sealed value); enums/sealed types are
 * stored as `TEXT` via `Converters`. `sectionId` FKs to [SectionEntity] with `SET_NULL` on delete — deletion
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
    val dueTime: Int? = null,
    val recurrence: Recurrence = Recurrence.None,
    val reminderWarning: ReminderWarning = ReminderWarning.None,
    val completedAt: Long? = null,
    val createdAt: Long,
    val lastCompletedScheduledDate: Long? = null,
    val isPinned: Boolean = false,
    val pendingExtensionOriginalDueDate: Long? = null,
    val pendingExtensionCount: Int = 0,
)
