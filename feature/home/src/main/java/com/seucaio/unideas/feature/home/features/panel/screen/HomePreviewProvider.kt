package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import java.time.LocalDate
import java.time.LocalDateTime

class HomePreviewProvider : PreviewParameterProvider<HomeUiState> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)
    private val sections = listOf(Section(id = 1L, name = "Trabalho"), Section(id = 2L, name = "Casa"))
    private val tags = listOf(Tag(id = 1L, name = "urgente"), Tag(id = 2L, name = "pessoal"))

    private fun task(
        id: Long,
        title: String,
        dueDate: LocalDate?,
        recurrence: Recurrence = Recurrence.None,
    ): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        dueDate = dueDate,
        recurrence = recurrence,
        createdAt = createdAt,
    )

    override val values: Sequence<HomeUiState> = sequenceOf(
        HomeUiState.Loading,
        HomeUiState.Success(
            priorityItems = listOf(task(1L, "Pagar contas", LocalDate.of(2026, 6, 25))),
            showSeeAllButton = false,
            activeTab = ItemType.TASK,
            tabItems = listOf(
                task(1L, "Pagar contas", LocalDate.of(2026, 6, 25)),
                task(2L, "Renovar assinatura", LocalDate.of(2026, 7, 10), Recurrence.Monthly),
            ),
            sectionFilter = null,
            tagFilters = emptySet(),
            availableSections = sections,
            availableTags = tags,
            hasAnyItem = true,
        ),
        // filtered-to-zero: user has items elsewhere, just none match this tab/filter
        HomeUiState.Success(
            priorityItems = (1..6).map { task(it.toLong(), "Prioridade $it", LocalDate.of(2026, 6, 20)) },
            showSeeAllButton = true,
            activeTab = ItemType.NOTE,
            tabItems = emptyList(),
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
            sectionFilter = null,
            tagFilters = emptySet(),
            availableSections = emptyList(),
            availableTags = emptyList(),
            hasAnyItem = false,
        ),
        HomeUiState.Error(R.string.home_load_error),
    )
}
