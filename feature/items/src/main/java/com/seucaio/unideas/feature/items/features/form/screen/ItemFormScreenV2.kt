package com.seucaio.unideas.feature.items.features.form.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.inputs.AppTextField
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.screen.components.DueDateField
import com.seucaio.unideas.feature.items.features.form.screen.components.RecurrenceField
import com.seucaio.unideas.feature.items.features.form.screen.components.SectionField
import com.seucaio.unideas.feature.items.features.form.screen.components.TagsField
import com.seucaio.unideas.feature.items.features.form.screen.components.TypeSelectorField
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiAction
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiState
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * #86 Pacote 2, direção V2: renders as a [ModalBottomSheet] (`skipPartiallyExpanded = true`, so it
 * opens fully expanded like a screen) instead of a regular destination — the other visual variant
 * under comparison, alongside V3/V4's plain `Scaffold` destinations. No top bar — the sheet's own
 * dismiss (drag/back/outside tap) covers "voltar", and the Save action lives as a full-width
 * [Button] at the end of the form instead of a toolbar action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreenV2(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemFormUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is ItemFormUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemFormUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { updatedOnNavigateBack?.invoke() },
        sheetState = sheetState,
    ) {
        ItemFormV2Content(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun ItemFormV2Content(
    uiState: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Column {
        ItemFormV2Body(state = uiState, onEvent = onEvent)
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun ItemFormV2Body(
    state: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        TypeSelectorField(type = state.type, onEvent = onEvent)

        FormField(label = stringResource(R.string.item_form_title_label), modifier = Modifier.padding(top = 16.dp)) {
            AppTextField(
                value = state.title,
                onValueChange = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
                placeholder = stringResource(R.string.item_form_title_label),
            )
        }

        FormField(
            label = stringResource(R.string.item_form_description_label),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            AppTextField(
                value = state.description,
                onValueChange = { onEvent(ItemFormEvent.OnDescriptionChanged(it)) },
                placeholder = stringResource(R.string.item_form_description_label),
                singleLine = false,
                minHeight = 96.dp,
            )
        }

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

        DueDateField(dueDate = state.dueDate, onEvent = onEvent, modifier = Modifier.padding(top = 16.dp))

        if (state.canPickRecurrence) {
            RecurrenceField(
                recurrence = state.recurrence,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
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

@PreviewLightDark
@Composable
private fun ItemFormScreenV2Preview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        Surface {
            ItemFormV2Content(
                uiState = previewState.uiState,
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
