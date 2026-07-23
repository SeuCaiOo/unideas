package com.seucaio.unideas.feature.items.ui.screens.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState

class ItemDetailPreviewProvider : PreviewParameterProvider<com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState> {

    private val sections = listOf(Section(id = 1L, name = "Work"), Section(id = 2L, name = "Home"))
    private val tags = listOf(Tag(id = 1L, name = "urgent"), Tag(id = 2L, name = "personal"))

    override val values: Sequence<com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState> =
        sequenceOf(
            // Sheet right after opening, before the reference-data load resolves — the form is
            // already fully usable, no spinner blocking it.
            _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState(
                type = ItemType.NOTE
            ),
            _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState(
                type = ItemType.NOTE,
                title = "Pay bills",
                description = "Electricity and water",
                availableSections = sections,
                availableTags = tags,
            ),
        )
}
