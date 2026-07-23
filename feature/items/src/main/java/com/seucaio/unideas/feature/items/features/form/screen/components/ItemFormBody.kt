package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.inputs.AppTextField
import com.seucaio.unideas.ds.components.inputs.BorderlessTextField
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.screen.ItemFormPreviewProvider
import com.seucaio.unideas.feature.items.features.form.screen.ItemFormPreviewState
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiState

/**
 * Field set + save action common to every `ItemFormScreen*` POC (#86/#97): type selector, title,
 * description, section/tags/due-date/recurrence, and a full-width save button at the end. The only
 * point of variation across POCs is how title/description render — [titleDescriptionFields] lets a
 * caller (V3) swap in its own styling while keeping everything else (order, conditions, save
 * action) identical everywhere else. Deliberately doesn't own the screen chrome around it
 * (`Scaffold`/`topBar`/`ModalBottomSheet`) — that stays specific to each screen.
 */
@Composable
fun ItemFormBody(
    state: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    titleDescriptionFields: @Composable (ItemFormUiState, (ItemFormEvent) -> Unit) -> Unit = { s, e ->
        DefaultTitleDescriptionFields(state = s, onEvent = e)
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        titleDescriptionFields(state, onEvent)

        TypeSelectorField(
            type = state.type,
            onEvent = onEvent,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (state.availableSections.isNotEmpty()) {
            SectionField(
                availableSections = state.availableSections,
                sectionId = state.sectionId,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.availableTags.isNotEmpty()) {
            TagsField(
                availableTags = state.availableTags,
                selectedTagIds = state.selectedTagIds,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.typeIsTask) {
            DueDateField(
                dueDate = state.dueDate,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (state.canPickRecurrence) {
                RecurrenceField(
                    recurrence = state.recurrence,
                    onEvent = onEvent,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        Button(
            onClick = { onEvent(ItemFormEvent.OnSaveClicked) },
            enabled = state.isTitleValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text(stringResource(R.string.item_form_save))
        }
    }
}

/** Default title/description rendering shared by V1/V2/V4 — outlined [AppTextField] under a [FormField] label. */
@Composable
private fun DefaultTitleDescriptionFields(
    state: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit
) {
    BorderlessTextField(
        value = state.title,
        onValueChange = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
        placeholder = stringResource(R.string.item_form_title_label),
        textStyle = MaterialTheme.typography.headlineLarge,
    )

    BorderlessTextField(
        value = state.description,
        onValueChange = { onEvent(ItemFormEvent.OnDescriptionChanged(it)) },
        placeholder = stringResource(R.string.item_form_description_label),
        singleLine = false,
        minHeight = 96.dp,
        textStyle = MaterialTheme.typography.titleLarge,
    )
}

@PreviewLightDark
@Composable
private fun ItemFormBodyPreview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        Surface {
            ItemFormBody(state = previewState.uiState, onEvent = {})
        }
    }
}
