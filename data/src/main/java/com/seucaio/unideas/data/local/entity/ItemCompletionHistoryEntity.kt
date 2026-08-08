package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the `item_completion_history` table — one row per recurring [ItemEntity]
 * occurrence. `itemId` FKs to [ItemEntity] with `CASCADE` on delete, unlike `items.sectionId`'s
 * `SET_NULL`: a history row is meaningless once its item is gone.
 */
@Entity(
    tableName = "item_completion_history",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["itemId"])],
)
data class ItemCompletionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val itemId: Long,
    val scheduledDate: Long,
    val completedAt: Long?,
    val note: String? = null,
)
