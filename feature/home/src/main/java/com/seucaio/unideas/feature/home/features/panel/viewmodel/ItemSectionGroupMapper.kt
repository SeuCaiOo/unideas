package com.seucaio.unideas.feature.home.features.panel.viewmodel

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.Section

/**
 * Groups by [Item.sectionId], ordered to match [sections] (creation order from
 * [com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase]), with an unsectioned bucket
 * ([ItemSectionGroup.sectionName] `null`) appended last if any item has no section. Sections with
 * no items in this list are omitted — an empty group would have nothing to render.
 */
internal fun List<Item>.groupBySection(sections: List<Section>): List<ItemSectionGroup> {
    val bySectionId = groupBy { it.sectionId }
    val named = sections.mapNotNull { section ->
        bySectionId[section.id]?.let { items -> ItemSectionGroup(section.id, section.name, items) }
    }
    val unsectioned = bySectionId[null]?.let { items ->
        ItemSectionGroup(sectionId = null, sectionName = null, items = items)
    }
    return if (unsectioned != null) named + unsectioned else named
}
