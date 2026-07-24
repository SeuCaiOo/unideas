package com.seucaio.unideas.feature.items.ui.screens.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState
import java.time.LocalDate

class ItemDetailPreviewProvider : PreviewParameterProvider<ItemDetailUiState> {

    private val sections = listOf(Section(id = 1L, name = "Work"), Section(id = 2L, name = "Home"))
    private val tags = listOf(Tag(id = 1L, name = "urgent"), Tag(id = 2L, name = "personal"))

    override val values: Sequence<ItemDetailUiState> = sequenceOf(
        // Screen right after opening, before the reference-data load resolves — the form is
        // already fully usable, no spinner blocking it.
        ItemDetailUiState(isEditing = false, type = ItemType.NOTE),
        ItemDetailUiState(
            type = ItemType.NOTE,
            title = "Pay bills",
            description = "Electricity and water",
            isEditing = false,
            availableSections = sections,
            availableTags = tags,
        ),
        ItemDetailUiState(isEditing = false),
        ItemDetailUiState(
            isEditing = true,
            title = "Pay bills",
            description = "Electricity and water",
            sectionId = 1L,
            selectedTagIds = setOf(1L),
            dueDate = LocalDate.of(2026, 7, 1),
            availableSections = sections,
            availableTags = tags,
        ),
    )
}
