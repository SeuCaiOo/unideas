package com.seucaio.unideas.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.entity.SectionEntity
import com.seucaio.unideas.data.local.entity.TagEntity

/**
 * An item with its tags (via the `item_tag` junction) and its section, both joined by Room —
 * never composed in memory. Backs the read-only detail screen, which needs the section's
 * `name`, not just [ItemEntity.sectionId].
 */
data class ItemWithTagsAndSection(
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
    @Relation(
        parentColumn = "sectionId",
        entityColumn = "id",
    )
    val section: SectionEntity?,
)
