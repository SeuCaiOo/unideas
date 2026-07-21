package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemSectionGroup
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsViewMode
import java.time.LocalDate
import java.time.LocalDateTime

class HomePreviewProvider : PreviewParameterProvider<HomeUiState> {

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

    override val values: Sequence<HomeUiState> = sequenceOf(
        HomeUiState.Loading,
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
            HomeUiState.Success(
                priorityItems = listOf(workItems.first()),
                showSeeAllButton = false,
                activeTab = ItemType.TASK,
                tabItems = allItems,
                groupedTabItems = listOf(
                    ItemSectionGroup(work.id, work.name, workItems),
                    ItemSectionGroup(home.id, home.name, homeItems),
                    ItemSectionGroup(sectionId = null, sectionName = null, items = unsectionedItems),
                ),
                sectionFilter = null,
                tagFilters = emptySet(),
                availableSections = sections,
                availableTags = tags,
                hasAnyItem = true,
                viewMode = ItemsViewMode.LIST,
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
            HomeUiState.Success(
                priorityItems = emptyList(),
                showSeeAllButton = false,
                activeTab = ItemType.TASK,
                tabItems = filteredItems,
                groupedTabItems = listOf(ItemSectionGroup(home.id, home.name, filteredItems)),
                sectionFilter = home.id,
                tagFilters = emptySet(),
                availableSections = sections,
                availableTags = tags,
                hasAnyItem = true,
                viewMode = ItemsViewMode.LIST,
            )
        },
        // filtered-to-zero: user has items elsewhere, just none match this tab/filter
        HomeUiState.Success(
            priorityItems = (1..6).map { task(it.toLong(), "Prioridade $it", LocalDate.of(2026, 6, 20)) },
            showSeeAllButton = true,
            activeTab = ItemType.NOTE,
            tabItems = emptyList(),
            groupedTabItems = emptyList(),
            sectionFilter = 1L,
            tagFilters = setOf(1L),
            availableSections = sections,
            availableTags = tags,
            hasAnyItem = true,
            viewMode = ItemsViewMode.LIST,
        ),
        // true first-run empty state: no items anywhere in the app
        HomeUiState.Success(
            priorityItems = emptyList(),
            showSeeAllButton = false,
            activeTab = ItemType.TASK,
            tabItems = emptyList(),
            groupedTabItems = emptyList(),
            sectionFilter = null,
            tagFilters = emptySet(),
            availableSections = emptyList(),
            availableTags = emptyList(),
            hasAnyItem = false,
            viewMode = ItemsViewMode.LIST,
        ),
        HomeUiState.Error(R.string.home_load_error),
    )
}
