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
        run {
            val workItem = task(1L, "Pagar contas", LocalDate.of(2026, 6, 25), sectionId = work.id)
            val homeItem =
                task(2L, "Renovar assinatura", LocalDate.of(2026, 7, 10), Recurrence.Monthly, sectionId = home.id)
            val unsectionedItem = task(3L, "Ler artigo", LocalDate.of(2026, 7, 15))
            HomeUiState.Success(
                priorityItems = listOf(workItem),
                showSeeAllButton = false,
                activeTab = ItemType.TASK,
                tabItems = listOf(workItem, homeItem, unsectionedItem),
                groupedTabItems = listOf(
                    ItemSectionGroup(work.id, work.name, listOf(workItem)),
                    ItemSectionGroup(home.id, home.name, listOf(homeItem)),
                    ItemSectionGroup(sectionId = null, sectionName = null, items = listOf(unsectionedItem)),
                ),
                sectionFilter = null,
                tagFilters = emptySet(),
                availableSections = sections,
                availableTags = tags,
                hasAnyItem = true,
            )
        },
        // filtered-to-one-section: exercises the flat (non-grouped, no header) list branch
        run {
            val filteredItem = task(4L, "Consertar torneira", LocalDate.of(2026, 6, 30), sectionId = home.id)
            HomeUiState.Success(
                priorityItems = emptyList(),
                showSeeAllButton = false,
                activeTab = ItemType.TASK,
                tabItems = listOf(filteredItem),
                groupedTabItems = listOf(ItemSectionGroup(home.id, home.name, listOf(filteredItem))),
                sectionFilter = home.id,
                tagFilters = emptySet(),
                availableSections = sections,
                availableTags = tags,
                hasAnyItem = true,
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
        ),
        HomeUiState.Error(R.string.home_load_error),
    )
}
