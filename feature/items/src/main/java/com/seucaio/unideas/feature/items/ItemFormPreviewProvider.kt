package com.seucaio.unideas.feature.items

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.viewmodel.ItemFormUiState
import java.time.LocalDate

data class ItemFormPreviewState(
    val isEditing: Boolean,
    val uiState: ItemFormUiState,
)

class ItemFormPreviewProvider : PreviewParameterProvider<ItemFormPreviewState> {

    private val sections = listOf(Section(id = 1L, name = "Work"), Section(id = 2L, name = "Home"))
    private val tags = listOf(Tag(id = 1L, name = "urgent"), Tag(id = 2L, name = "personal"))

    override val values: Sequence<ItemFormPreviewState> = sequenceOf(
        ItemFormPreviewState(isEditing = false, uiState = ItemFormUiState.Loading),
        ItemFormPreviewState(
            isEditing = false,
            uiState = ItemFormUiState.Success(
                isEditing = false,
                availableSections = sections,
                availableTags = tags,
            ),
        ),
        ItemFormPreviewState(
            isEditing = true,
            uiState = ItemFormUiState.Success(
                isEditing = true,
                type = ItemType.TASK,
                title = "Pay bills",
                description = "Electricity and water",
                sectionId = 1L,
                selectedTagIds = setOf(1L),
                dueDate = LocalDate.of(2026, 7, 1),
                availableSections = sections,
                availableTags = tags,
            ),
        ),
        ItemFormPreviewState(
            isEditing = true,
            uiState = ItemFormUiState.Error(messageRes = R.string.item_form_load_error)
        ),
    )
}
