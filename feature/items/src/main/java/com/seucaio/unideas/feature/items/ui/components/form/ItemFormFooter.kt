package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.CompletionField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState
import java.time.LocalDateTime

/**
 * The form's two closing actions: "Mark as completed" — only for a task already being edited —
 * and "Save" — only while creating, since editing an existing item auto-saves every field already
 * (see [com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel]).
 */
@Composable
fun ItemFormFooter(state: ItemFormFieldsState, events: ItemFormFieldsEvents, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (state.typeIsTask && state.isEditing) {
            CompletionField(
                isCompleted = state.isCompleted,
                completedAt = state.completedAt,
                onCompleteClicked = events.onCompleteClicked,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (!state.isEditing) {
            Button(
                onClick = events.onSaveClicked,
                enabled = state.isTitleValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text(stringResource(R.string.item_form_save))
            }
        }
    }
}

private val noopEvents = ItemFormFieldsEvents(
    onTypeChanged = {},
    onTitleChanged = {},
    onDescriptionChanged = {},
    onSectionChanged = {},
    onTagToggled = {},
    onReminderToggled = {},
    onDueDateChanged = {},
    onDueTimeChanged = {},
    onRecurrenceChanged = {},
    onReminderWarningChanged = {},
    onSaveClicked = {},
)

/** Editing an existing task: only "Mark as completed" — no "Save", already auto-saved. */
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
                events = noopEvents,
            )
        }
    }
}

/** A completed task keeps showing the completion toggle (now checked), still no "Save". */
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
                    isCompleted = true,
                    completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
                ),
                events = noopEvents,
            )
        }
    }
}

/** Note, or a task being created (not editing yet): only Save. */
@PreviewLightDark
@Composable
private fun ItemFormFooterNoteOrNewItemPreview() {
    UdsTheme {
        Surface {
            ItemFormFooter(
                state = ItemDetailUiState(type = ItemType.NOTE, title = "Groceries"),
                events = noopEvents,
            )
        }
    }
}
