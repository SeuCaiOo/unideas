package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.ui.components.fields.TitleDescriptionFields
import com.seucaio.unideas.feature.items.ui.components.fields.TypeSelectorField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState

/**
 * Composes the item form's four parts, top to bottom: title/description, type selector, the
 * collapsed [ItemFormOptionsSection] (section/tags/due fields), and [ItemFormFooter] (completion +
 * save). Each part owns its own visibility rules — this just lays them out in order.
 */
@Composable
fun ItemFormBody(state: ItemFormFieldsState, events: ItemFormFieldsEvents, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
    ) {
        TitleDescriptionFields(
            title = state.title,
            description = state.description,
            onTitleChanged = events.onTitleChanged,
            onDescriptionChanged = events.onDescriptionChanged,
            isEditing = state.isEditing,
            titleError = state.titleError,
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            TypeSelectorField(
                type = state.type,
                onTypeChanged = events.onTypeChanged,
                modifier = Modifier.padding(top = 16.dp),
            )

            ItemFormOptionsSection(
                state = state,
                events = events,
                modifier = Modifier.padding(top = 16.dp)
            )

            ItemFormFooter(state = state, events = events)
        }
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
