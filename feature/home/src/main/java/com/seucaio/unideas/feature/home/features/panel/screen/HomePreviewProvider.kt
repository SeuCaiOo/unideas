package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.home.features.panel.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemSectionGroup
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsViewMode
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A `Success`-shaped fixture (#102, 2026-07-22) — [HomeViewModel][com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel]
 * exposes [ItemsState]/[FilterState]/`hasAnyItem` as three independent `StateFlow`s instead of one
 * combined type, so previews need all three together to render a screen/component the same way
 * the real Screen would compose them.
 */
internal data class HomePreviewFixture(
    val itemsState: ItemsState,
    val filterState: FilterState,
    val hasAnyItem: Boolean,
)

internal class HomePreviewProvider : PreviewParameterProvider<HomePreviewFixture> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)
    private val work = Section(id = 1L, name = "Trabalho")
    private val home = Section(id = 2L, name = "Casa")
    private val sections = listOf(work, home)
    private val tags = listOf(Tag(id = 1L, name = "urgente"), Tag(id = 2L, name = "pessoal"))

    private fun task(
        id: Long,
        title: String,
        dueDate: LocalDate?,
        recurrence: Recurrence = Recurrence.None,
        sectionId: Long? = null,
    ): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        dueDate = dueDate,
        recurrence = recurrence,
        createdAt = createdAt,
        sectionId = sectionId,
    )

    override val values: Sequence<HomePreviewFixture> = sequenceOf(
        // grouped, multiple items per group: 2+ items in "Trabalho"/"Casa" so the grid view
        // actually shows 2 columns side by side within a group, not just one card per row —
        // a single-item-per-group fixture can't tell a broken 2-column grid from a correct one.
        run {
            val workItems = listOf(
                task(1L, "Pagar contas", LocalDate.of(2026, 6, 25), sectionId = work.id),
                task(5L, "Enviar relatório", LocalDate.of(2026, 6, 28), sectionId = work.id),
            )
            val homeItems = listOf(
                task(2L, "Renovar assinatura", LocalDate.of(2026, 7, 10), Recurrence.Monthly, sectionId = home.id),
                task(6L, "Consertar torneira", LocalDate.of(2026, 6, 30), sectionId = home.id),
            )
            // odd count on purpose — last row of the grid should show one card + one empty cell
            val unsectionedItems = listOf(task(3L, "Ler artigo", LocalDate.of(2026, 7, 15)))
            val allItems = workItems + homeItems + unsectionedItems
            HomePreviewFixture(
                itemsState = ItemsState(
                    priorityItems = listOf(workItems.first()),
                    showSeeAllButton = false,
                    tabItems = allItems,
                    groupedTabItems = listOf(
                        ItemSectionGroup(work.id, work.name, workItems),
                        ItemSectionGroup(home.id, home.name, homeItems),
                        ItemSectionGroup(sectionId = null, sectionName = null, items = unsectionedItems),
                    ),
                ),
                filterState = FilterState(
                    activeTab = ItemType.TASK,
                    sectionFilter = null,
                    tagFilters = emptySet(),
                    availableSections = sections,
                    availableTags = tags,
                    viewMode = ItemsViewMode.LIST,
                ),
                hasAnyItem = true,
            )
        },
        // filtered-to-one-section, multiple items: exercises the flat (non-grouped, no header)
        // branch in both LIST and GRID modes with more than one card, same reasoning as above
        run {
            val filteredItems = listOf(
                task(4L, "Consertar torneira", LocalDate.of(2026, 6, 30), sectionId = home.id),
                task(7L, "Renovar assinatura", LocalDate.of(2026, 7, 10), Recurrence.Monthly, sectionId = home.id),
                task(8L, "Organizar armário", null, sectionId = home.id),
            )
            HomePreviewFixture(
                itemsState = ItemsState(
                    priorityItems = emptyList(),
                    showSeeAllButton = false,
                    tabItems = filteredItems,
                    groupedTabItems = listOf(ItemSectionGroup(home.id, home.name, filteredItems)),
                ),
                filterState = FilterState(
                    activeTab = ItemType.TASK,
                    sectionFilter = home.id,
                    tagFilters = emptySet(),
                    availableSections = sections,
                    availableTags = tags,
                    viewMode = ItemsViewMode.LIST,
                ),
                hasAnyItem = true,
            )
        },
        // filtered-to-zero: user has items elsewhere, just none match this tab/filter
        HomePreviewFixture(
            itemsState = ItemsState(
                priorityItems = (1..6).map { task(it.toLong(), "Prioridade $it", LocalDate.of(2026, 6, 20)) },
                showSeeAllButton = true,
                tabItems = emptyList(),
                groupedTabItems = emptyList(),
            ),
            filterState = FilterState(
                activeTab = ItemType.NOTE,
                sectionFilter = 1L,
                tagFilters = setOf(1L),
                availableSections = sections,
                availableTags = tags,
                viewMode = ItemsViewMode.LIST,
            ),
            hasAnyItem = true,
        ),
        // true first-run empty state: no items anywhere in the app
        HomePreviewFixture(
            itemsState = ItemsState(),
            filterState = FilterState(),
            hasAnyItem = false,
        ),
    )
}
