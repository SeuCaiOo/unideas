package com.seucaio.unideas.feature.items.features.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailUiState

class ItemDetailPreviewProvider : PreviewParameterProvider<ItemDetailUiState> {

    private val sections = listOf(Section(id = 1L, name = "Work"), Section(id = 2L, name = "Home"))
    private val tags = listOf(Tag(id = 1L, name = "urgent"), Tag(id = 2L, name = "personal"))

    override val values: Sequence<ItemDetailUiState> = sequenceOf(
        // Sheet right after opening, before the reference-data load resolves — the form is
        // already fully usable, no spinner blocking it.
        ItemDetailUiState(type = ItemType.NOTE),
        ItemDetailUiState(
            type = ItemType.NOTE,
            title = "Pay bills",
            description = "Electricity and water",
            availableSections = sections,
            availableTags = tags,
        ),
    )
}
