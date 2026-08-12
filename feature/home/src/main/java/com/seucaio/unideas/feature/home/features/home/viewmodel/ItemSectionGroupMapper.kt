package com.seucaio.unideas.feature.home.features.home.viewmodel

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.Section

internal fun List<Item>.groupBySection(sections: List<Section>): List<ItemSectionGroup> {
    val bySectionId = groupBy { it.sectionId }
    val named = sections.mapNotNull { section ->
        bySectionId[section.id]?.let { items ->
            ItemSectionGroup(
                sectionId = section.id,
                sectionName = section.name,
                items = items.sortedByDescending { it.isPinned },
                isPinned = section.isPinned
            )
        }
    }
    val unsectionedItems = bySectionId[null].orEmpty()
    val pinnedItemsGroup =
        unsectionedItems.filter { it.isPinned }.takeIf { it.isNotEmpty() }?.let { items ->
            ItemSectionGroup(
                sectionId = null,
                sectionName = null,
                items = items,
                isPinned = true,
                isPinnedItemsGroup = true
            )
        }
    val unsectioned =
        unsectionedItems.filterNot { it.isPinned }.takeIf { it.isNotEmpty() }?.let { items ->
            ItemSectionGroup(sectionId = null, sectionName = null, items = items)
        }
    return listOfNotNull(pinnedItemsGroup) + named + listOfNotNull(unsectioned)
}
