package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.ui.components.fields.CompletionField
import com.seucaio.unideas.feature.items.ui.components.fields.OverdueOccurrenceActions
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiState
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun ItemFormFooter(
    state: ItemFormFieldsState,
    occurrenceState: ItemOccurrenceUiState,
    onCompleteClicked: () -> Unit,
    onIgnoreClicked: () -> Unit,
    onExtendDeadlineClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (state.typeIsTask && state.isEditing) {
            CompletionField(
                isCompleted = occurrenceState.isCompleted,
                isLate = occurrenceState.isLate,
                completedAt = occurrenceState.completedAt,
                onCompleteClicked = onCompleteClicked,
                modifier = Modifier.padding(top = 16.dp),
            )

            if (occurrenceState.isLate) {
                OverdueOccurrenceActions(
                    onExtendDeadlineClicked = onExtendDeadlineClicked,
                    onIgnoreClicked = if (occurrenceState.canIgnore) onIgnoreClicked else null,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private data class ItemFormFooterPreviewData(
    val state: ItemFormFieldsState,
    val occurrenceState: ItemOccurrenceUiState,
)

private class ItemFormFooterPreviewProvider : PreviewParameterProvider<ItemFormFooterPreviewData> {

    override val values: Sequence<ItemFormFooterPreviewData> = sequenceOf(
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Pay bills", isEditing = true),
            occurrenceState = ItemOccurrenceUiState(),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Pay bills", isEditing = true),
            occurrenceState = ItemOccurrenceUiState(
                isCompleted = true,
                completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
            ),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Water the plants", isEditing = true),
            occurrenceState = ItemOccurrenceUiState(
                dueDate = LocalDate.now().minusDays(3),
                isRecurring = true,
            ),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Renew subscription", isEditing = true),
            occurrenceState = ItemOccurrenceUiState(
                dueDate = LocalDate.now().minusDays(3),
                isRecurring = false,
            ),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.NOTE, title = "Groceries"),
            occurrenceState = ItemOccurrenceUiState(),
        ),
    )
}

@PreviewLightDark
@Composable
private fun ItemFormFooterPreview(
    @PreviewParameter(ItemFormFooterPreviewProvider::class) previewData: ItemFormFooterPreviewData,
) {
    UdsTheme {
        Surface {
            ItemFormFooter(
                state = previewData.state,
                occurrenceState = previewData.occurrenceState,
                onCompleteClicked = {},
                onIgnoreClicked = {},
                onExtendDeadlineClicked = {},
            )
        }
    }
}
