package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.SwitchSection
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.DueDateField
import com.seucaio.unideas.feature.items.ui.components.fields.DueTimeField
import com.seucaio.unideas.feature.items.ui.components.fields.ReminderWarningField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.components.fields.recurrence.RecurrenceField
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState

@Composable
fun ItemFormTaskOptions(
    state: ItemFormFieldsState,
    events: ItemFormFieldsEvents,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SwitchSection(
            label = stringResource(R.string.item_form_has_reminder_label),
            checked = state.hasReminder,
            onCheckedChange = events.onReminderToggled,
        ) {
            RecurrenceField(
                recurrence = state.recurrence,
                dueDate = state.dueDate,
                onRecurrenceChanged = events.onRecurrenceChanged,
                onDueDateChanged = events.onDueDateChanged,
                modifier = Modifier.padding(top = 16.dp),
            )

            if (state.recurrence == Recurrence.None) {
                DueDateField(
                    dueDate = state.dueDate,
                    onDueDateChanged = events.onDueDateChanged,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            DueTimeField(
                dueTime = state.dueTime,
                onDueTimeChanged = events.onDueTimeChanged,
                modifier = Modifier.padding(top = 16.dp),
            )

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
                    onTitleChanged = {},
                    onDescriptionChanged = {},
                    onSectionChanged = {},
                    onTagToggled = {},
                    onReminderToggled = {},
                    onDueDateChanged = {},
                    onDueTimeChanged = {},
                    onRecurrenceChanged = {},
                    onReminderWarningChanged = {},
                ),
            )
        }
    }
}
