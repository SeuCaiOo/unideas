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
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
        if (state.typeIsTask) {
            CompletionField(
                isCompleted = occurrenceState.isCompleted,
                isLate = occurrenceState.isLate,
                completedLate = occurrenceState.completedLate,
                completedAt = occurrenceState.completedAt,
                overdueDays = occurrenceState.dueDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() },
                onCompleteClicked = onCompleteClicked,
                onExtendDeadlineClicked = onExtendDeadlineClicked,
                onIgnoreClicked = if (occurrenceState.canIgnore) onIgnoreClicked else null,
                modifier = Modifier.padding(top = 16.dp),
            )
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
            state = ItemDetailUiState(type = ItemType.TASK, title = "Pay bills", itemId = 1L),
            occurrenceState = ItemOccurrenceUiState(),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Pay bills", itemId = 1L),
            occurrenceState = ItemOccurrenceUiState(
                isCompleted = true,
                completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
            ),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Water the plants", itemId = 2L),
            occurrenceState = ItemOccurrenceUiState(
                dueDate = LocalDate.now().minusDays(3),
                isRecurring = true,
            ),
        ),
        ItemFormFooterPreviewData(
            state = ItemDetailUiState(type = ItemType.TASK, title = "Renew subscription", itemId = 3L),
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
