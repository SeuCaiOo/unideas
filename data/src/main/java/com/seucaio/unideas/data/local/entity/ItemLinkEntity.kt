package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Symmetric N:N link between two [ItemEntity]s (`item_link` table) — no direction, mirrors
 * [ItemTagCrossRef]'s shape. [itemIdA] is always the smaller id (see `ItemLinkDao`'s insert),
 * so a link is never stored twice as both A→B and B→A.
 *
 * Composite PK (`itemIdA`, `itemIdB`); both FKs cascade on delete, so removing either item
 * removes the link automatically.
 */
@Entity(
    tableName = "item_link",
    primaryKeys = ["itemIdA", "itemIdB"],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemIdA"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemIdB"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["itemIdB"])],
)
data class ItemLinkEntity(
    val itemIdA: Long,
    val itemIdB: Long,
)
