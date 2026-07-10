package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * N:N junction between [ItemEntity] and [TagEntity] (`item_tag` table).
 *
 * Composite PK (`itemId`, `tagId`); both FKs cascade on delete, so removing
 * an item (or a tag) removes its links automatically.
 */
@Entity(
    tableName = "item_tag",
    primaryKeys = ["itemId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["tagId"])],
)
data class ItemTagCrossRef(
    val itemId: Long,
    val tagId: Long,
)
