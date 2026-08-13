package com.seucaio.unideas.feature.home.features.home.viewmodel

import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.SectionStub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemSectionGroupMapperTest {

    @Test
    fun `when an unsectioned item is pinned should move it into its own pinned items group`() {
        val pinned = ItemStub.task(id = 1L, sectionId = null, isPinned = true)
        val unpinned = ItemStub.task(id = 2L, sectionId = null, isPinned = false)

        val groups = listOf(pinned, unpinned).groupBySection(sections = emptyList())

        val pinnedItemsGroup = groups.single { it.isPinnedItemsGroup }
        assertEquals(listOf(pinned), pinnedItemsGroup.items)
        assertNull(pinnedItemsGroup.sectionId)
        val noSectionGroup = groups.single { !it.isPinnedItemsGroup }
        assertEquals(listOf(unpinned), noSectionGroup.items)
    }

    @Test
    fun `when a sectioned item is pinned should stay in its section group sorted first`() {
        val section = SectionStub.section(id = 1L)
        val pinned = ItemStub.task(id = 1L, sectionId = 1L, isPinned = true)
        val unpinned = ItemStub.task(id = 2L, sectionId = 1L, isPinned = false)

        val groups = listOf(unpinned, pinned).groupBySection(sections = listOf(section))

        assertEquals(1, groups.size)
        assertEquals(listOf(pinned, unpinned), groups.single().items)
        assertTrue(groups.none { it.isPinnedItemsGroup })
    }

    @Test
    fun `when there are no unsectioned pinned items should omit the pinned items group`() {
        val item = ItemStub.task(id = 1L, sectionId = null, isPinned = false)

        val groups = listOf(item).groupBySection(sections = emptyList())

        assertTrue(groups.none { it.isPinnedItemsGroup })
    }

    @Test
    fun `groups are ordered pinned items group, then sections in order, then unsectioned`() {
        val pinnedSection = SectionStub.section(id = 1L, name = "Pinned section", isPinned = true)
        val normalSection = SectionStub.section(id = 2L, name = "Normal section")
        val pinnedUnsectioned = ItemStub.task(id = 1L, sectionId = null, isPinned = true)
        val itemInPinnedSection = ItemStub.task(id = 2L, sectionId = 1L)
        val itemInNormalSection = ItemStub.task(id = 3L, sectionId = 2L)
        val unsectioned = ItemStub.task(id = 4L, sectionId = null, isPinned = false)

        val groups = listOf(unsectioned, itemInNormalSection, itemInPinnedSection, pinnedUnsectioned)
            .groupBySection(sections = listOf(pinnedSection, normalSection))

        assertEquals(
            listOf(true, false, false, false),
            groups.map { it.isPinnedItemsGroup },
        )
        assertEquals(
            listOf(null, 1L, 2L, null),
            groups.map { it.sectionId },
        )
    }
}
