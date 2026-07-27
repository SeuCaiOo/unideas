package com.seucaio.unideas.feature.items.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.CompletionField
import com.seucaio.unideas.feature.items.ui.components.fields.DueDateField
import com.seucaio.unideas.feature.items.ui.components.fields.DueTimeField
import com.seucaio.unideas.feature.items.ui.components.fields.RecurrenceField
import com.seucaio.unideas.feature.items.ui.components.fields.ReminderWarningField
import com.seucaio.unideas.feature.items.ui.components.fields.SectionField
import com.seucaio.unideas.feature.items.ui.components.fields.TagsField
import com.seucaio.unideas.feature.items.ui.components.fields.TitleDescriptionFields
import com.seucaio.unideas.feature.items.ui.components.fields.TypeSelectorField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState

@Composable
fun ItemFormBody(
    state: ItemFormFieldsState,
    events: ItemFormFieldsEvents,
    modifier: Modifier = Modifier,
    titleDescriptionFields: @Composable (
        title: String,
        description: String,
        onTitleChanged: (String) -> Unit,
        onDescriptionChanged: (String) -> Unit,
        isEditing: Boolean,
    ) -> Unit = { title, description, onTitleChanged, onDescriptionChanged, isEditing ->
        TitleDescriptionFields(
            title = title,
            description = description,
            onTitleChanged = onTitleChanged,
            onDescriptionChanged = onDescriptionChanged,
            isEditing = isEditing,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        titleDescriptionFields(
            state.title,
            state.description,
            events.onTitleChanged,
            events.onDescriptionChanged,
            state.isEditing,
        )

        TypeSelectorField(
            type = state.type,
            onTypeChanged = events.onTypeChanged,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (state.availableSections.isNotEmpty()) {
            SectionField(
                availableSections = state.availableSections,
                sectionId = state.sectionId,
                onSectionChanged = events.onSectionChanged,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.availableTags.isNotEmpty()) {
            TagsField(
                availableTags = state.availableTags,
                selectedTagIds = state.selectedTagIds,
                onTagToggled = events.onTagToggled,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.typeIsTask) {
            TaskDueFields(state, events)
        }

        if (state.typeIsTask && state.isEditing) {
            CompletionField(
                isCompleted = state.isCompleted,
                completedAt = state.completedAt,
                onCompleteClicked = events.onCompleteClicked,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

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

/** Due date/time, recurrence and reminder warning — only shown for tasks with a due date set. */
@Composable
private fun TaskDueFields(state: ItemFormFieldsState, events: ItemFormFieldsEvents) {
    DueDateField(
        dueDate = state.dueDate,
        onDueDateChanged = events.onDueDateChanged,
        modifier = Modifier.padding(top = 16.dp)
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

@PreviewLightDark
@Composable
private fun ItemFormBodyPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) previewState: ItemDetailUiState,
) {
    UdsTheme {
        Surface {
            ItemFormBody(
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
