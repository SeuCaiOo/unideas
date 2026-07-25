package com.seucaio.unideas.feature.items.ui.screens.additem

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemUiState

class AddItemPreviewProvider : PreviewParameterProvider<AddItemUiState> {

    private val sections = listOf(Section(id = 1L, name = "Work"), Section(id = 2L, name = "Home"))
    private val tags = listOf(Tag(id = 1L, name = "urgent"), Tag(id = 2L, name = "personal"))

    override val values: Sequence<AddItemUiState> = sequenceOf(
        // Sheet right after opening, before the reference-data load resolves — the form is
        // already fully usable, no spinner blocking it.
        AddItemUiState(type = ItemType.NOTE),
        AddItemUiState(
            type = ItemType.NOTE,
            title = "Pay bills",
            description = "Electricity and water",
            availableSections = sections,
            availableTags = tags,
        ),
    )
}
