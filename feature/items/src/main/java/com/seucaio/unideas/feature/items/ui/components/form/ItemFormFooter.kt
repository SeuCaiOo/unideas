package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.ui.components.fields.CompletionField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemOccurrenceUiState
import java.time.LocalDateTime

@Composable
fun ItemFormFooter(
    state: ItemFormFieldsState,
    occurrenceState: ItemOccurrenceUiState,
    onCompleteClicked: () -> Unit,
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
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormFooterTaskEditingPreview() {
    UdsTheme {
        Surface {
            ItemFormFooter(
                state = ItemDetailUiState(
                    type = ItemType.TASK,
                    title = "Pay bills",
                    isEditing = true,
                ),
                occurrenceState = ItemOccurrenceUiState(),
                onCompleteClicked = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormFooterTaskCompletedPreview() {
    UdsTheme {
        Surface {
            ItemFormFooter(
                state = ItemDetailUiState(
                    type = ItemType.TASK,
                    title = "Pay bills",
                    isEditing = true,
                ),
                occurrenceState = ItemOccurrenceUiState(
                    isCompleted = true,
                    completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
                ),
                onCompleteClicked = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormFooterNoteOrNewItemPreview() {
    UdsTheme {
        Surface {
            ItemFormFooter(
                state = ItemDetailUiState(type = ItemType.NOTE, title = "Groceries"),
                occurrenceState = ItemOccurrenceUiState(),
                onCompleteClicked = {},
            )
        }
    }
}
