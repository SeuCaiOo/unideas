package com.seucaio.unideas.feature.items.ui.screens.form

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormUiState
import java.time.LocalDate

data class ItemFormPreviewState(
    val isEditing: Boolean,
    val uiState: ItemFormUiState,
)

class ItemFormPreviewProvider : PreviewParameterProvider<ItemFormPreviewState> {

    private val sections = listOf(Section(id = 1L, name = "Work"), Section(id = 2L, name = "Home"))
    private val tags = listOf(Tag(id = 1L, name = "urgent"), Tag(id = 2L, name = "personal"))

    override val values: Sequence<ItemFormPreviewState> = sequenceOf(
        // Screen right after opening, before the reference-data load resolves — the form is
        // already fully usable, no spinner blocking it.
        ItemFormPreviewState(
            isEditing = false,
            uiState = ItemFormUiState(isEditing = false, type = ItemType.NOTE)
        ),
        ItemFormPreviewState(
            isEditing = false,
            uiState = ItemFormUiState(
                type = ItemType.NOTE,
                title = "Pay bills",
                description = "Electricity and water",
                isEditing = false,
                availableSections = sections,
                availableTags = tags,
            ),
        ),
        ItemFormPreviewState(isEditing = false, uiState = ItemFormUiState(isEditing = false)),
        ItemFormPreviewState(
            isEditing = true,
            uiState = ItemFormUiState(
                isEditing = true,
                title = "Pay bills",
                description = "Electricity and water",
                sectionId = 1L,
                selectedTagIds = setOf(1L),
                dueDate = LocalDate.of(2026, 7, 1),
                availableSections = sections,
                availableTags = tags,
            ),
        ),
    )
}
