package com.seucaio.unideas.feature.items.ui.screens.additem

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.ui.components.ItemFormBody
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemEvent
import com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemUiAction
import com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemUiState
import com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: AddItemViewModel = koinViewModel { parametersOf(initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is AddItemUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is AddItemUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is AddItemUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { updatedOnNavigateBack?.invoke() },
        sheetState = sheetState,
    ) {
        AddItemSheetContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun AddItemSheetContent(
    uiState: AddItemUiState,
    onEvent: (AddItemEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val fieldsEvents = remember(onEvent) {
        ItemFormFieldsEvents(
            onTypeChanged = { onEvent(AddItemEvent.OnTypeChanged(it)) },
            onTitleChanged = { onEvent(AddItemEvent.OnTitleChanged(it)) },
            onDescriptionChanged = { onEvent(AddItemEvent.OnDescriptionChanged(it)) },
            onSectionChanged = { onEvent(AddItemEvent.OnSectionChanged(it)) },
            onTagToggled = { onEvent(AddItemEvent.OnTagToggled(it)) },
            onDueDateChanged = { onEvent(AddItemEvent.OnDueDateChanged(it)) },
            onRecurrenceChanged = { onEvent(AddItemEvent.OnRecurrenceChanged(it)) },
            onSaveClicked = { onEvent(AddItemEvent.OnSaveClicked) },
        )
    }

    Column {
        ItemFormBody(state = uiState, events = fieldsEvents)
        SnackbarHost(hostState = snackbarHostState)
    }
}

@PreviewLightDark
@Composable
private fun AddItemSheetPreview(
    @PreviewParameter(AddItemPreviewProvider::class) previewState: AddItemUiState,
) {
    UdsTheme {
        Surface {
            AddItemSheetContent(
                uiState = previewState,
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
