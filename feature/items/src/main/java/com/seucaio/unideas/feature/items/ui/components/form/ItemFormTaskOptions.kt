package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.ui.components.fields.DueDateField
import com.seucaio.unideas.feature.items.ui.components.fields.DueTimeField
import com.seucaio.unideas.feature.items.ui.components.fields.RecurrenceField
import com.seucaio.unideas.feature.items.ui.components.fields.ReminderWarningField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState

/** Due date/time, recurrence and reminder warning — task-only, not present on notes. */
@Composable
fun ItemFormTaskOptions(
    state: ItemFormFieldsState,
    events: ItemFormFieldsEvents,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        DueDateField(
            dueDate = state.dueDate,
            onDueDateChanged = events.onDueDateChanged,
        )

        if (state.canPickReminder) {
            DueTimeField(
                dueTime = state.dueTime,
                onDueTimeChanged = events.onDueTimeChanged,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.canPickRecurrence) {
            RecurrenceField(
                recurrence = state.recurrence,
                onRecurrenceChanged = events.onRecurrenceChanged,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.canPickReminder) {
            ReminderWarningField(
                reminderWarning = state.reminderWarning,
                onReminderWarningChanged = events.onReminderWarningChanged,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormTaskOptionsPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) previewState: ItemDetailUiState,
) {
    UdsTheme {
        Surface {
            ItemFormTaskOptions(
                state = previewState,
                events = ItemFormFieldsEvents(
                    onTypeChanged = {},
                    onTitleChanged = {},
                    onDescriptionChanged = {},
                    onSectionChanged = {},
                    onTagToggled = {},
                    onDueDateChanged = {},
                    onDueTimeChanged = {},
                    onRecurrenceChanged = {},
                    onReminderWarningChanged = {},
                    onSaveClicked = {},
                ),
            )
        }
    }
}
