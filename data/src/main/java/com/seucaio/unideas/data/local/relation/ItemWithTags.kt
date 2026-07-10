package com.seucaio.unideas.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.entity.TagEntity

/**
 * An item with its tags, joined by Room via the `item_tag` junction —
 * never composed in memory.
 */
data class ItemWithTags(
    @Embedded
    val item: ItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemTagCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity>,
)
