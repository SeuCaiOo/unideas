package com.seucaio.unideas.feature.items.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.extensions.shareText
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.ItemActions
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.form.ItemFormBody
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailDialogState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiAction
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailViewModel
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.ExtendDeadlineDatePickerDialog
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.NoteConfirmDialog
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceDialogState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiAction
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private fun Item.toShareText(): String = buildString {
    appendLine(title)
    description?.let { appendLine(it) }
    dueDate?.let { appendLine(it.toFormattedDateString()) }
}

@Composable
fun ItemDetailScreen(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    onNavigateToHistory: (Long) -> Unit,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemDetailViewModel = koinViewModel { parametersOf(itemId, initialType) },
    occurrenceViewModel: ItemOccurrenceViewModel = koinViewModel { parametersOf(itemId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val occurrenceState by occurrenceViewModel.uiState.collectAsStateWithLifecycle()
    val occurrenceDialogState by occurrenceViewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemDetailUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is ItemDetailUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemDetailUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
                is ItemDetailUiAction.ShareText -> context.shareText(action.item.toShareText())
                is ItemDetailUiAction.ItemPersisted ->
                    occurrenceViewModel.onEvent(ItemOccurrenceEvent.OnItemUpdatedExternally(action.item))
            }
        }
    }

    LaunchedEffect(Unit) {
        occurrenceViewModel.uiAction.collect { action ->
            when (action) {
                is ItemOccurrenceUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemOccurrenceUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
                is ItemOccurrenceUiAction.ItemPersisted ->
                    viewModel.onEvent(ItemDetailEvent.OnItemUpdatedExternally(action.item))
            }
        }
    }

    ItemDetailScreenContent(
        itemId = itemId,
        uiState = uiState,
        dialogState = dialogState,
        occurrenceState = occurrenceState,
        occurrenceDialogState = occurrenceDialogState,
        onEvent = viewModel::onEvent,
        onOccurrenceEvent = occurrenceViewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToHistory = onNavigateToHistory,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailScreenContent(
    itemId: Long?,
    uiState: ItemDetailUiState,
    dialogState: ItemDetailDialogState,
    occurrenceState: ItemOccurrenceUiState,
    occurrenceDialogState: ItemOccurrenceDialogState,
    onEvent: (ItemDetailEvent) -> Unit,
    onOccurrenceEvent: (ItemOccurrenceEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onNavigateToHistory: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    BackHandler {
        onEvent(ItemDetailEvent.OnBackRequested)
    }

    val topBarNavigateBack = updatedOnNavigateBack?.let { { onEvent(ItemDetailEvent.OnBackRequested) } }

    val fieldsEvents = remember(onEvent) {
        ItemFormFieldsEvents(
            onTypeChanged = { onEvent(ItemDetailEvent.OnTypeChanged(it)) },
            onTitleChanged = { onEvent(ItemDetailEvent.OnTitleChanged(it)) },
            onDescriptionChanged = { onEvent(ItemDetailEvent.OnDescriptionChanged(it)) },
            onSectionChanged = { onEvent(ItemDetailEvent.OnSectionChanged(it)) },
            onTagToggled = { onEvent(ItemDetailEvent.OnTagToggled(it)) },
            onReminderToggled = { onEvent(ItemDetailEvent.OnReminderToggled(it)) },
            onDueDateChanged = { onEvent(ItemDetailEvent.OnDueDateChanged(it)) },
            onDueTimeChanged = { onEvent(ItemDetailEvent.OnDueTimeChanged(it)) },
            onRecurrenceChanged = { onEvent(ItemDetailEvent.OnRecurrenceChanged(it)) },
            onReminderWarningChanged = { onEvent(ItemDetailEvent.OnReminderWarningChanged(it)) },
        )
    }

    Scaffold(
        topBar = {
            UnideasTopBar(
                onNavigateBack = topBarNavigateBack,
                actions = { ItemDetailTopBarActions(itemId, uiState, onEvent, onNavigateToHistory) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.isLoading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
            uiState.loadFailed -> UnideasErrorContent(
                messageRes = R.string.item_form_load_error,
                onRetry = { onEvent(ItemDetailEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
            else -> ItemFormBody(
                state = uiState,
                events = fieldsEvents,
                occurrenceState = occurrenceState,
                onCompleteClicked = { onOccurrenceEvent(ItemOccurrenceEvent.OnCompleteClicked) },
                onIgnoreClicked = { onOccurrenceEvent(ItemOccurrenceEvent.OnIgnoreClicked) },
                onExtendDeadlineClicked = { onOccurrenceEvent(ItemOccurrenceEvent.OnExtendDeadlineClicked) },
                modifier = Modifier.padding(padding),
            )
        }
    }

    ItemDetailDialogs(dialogState, onEvent)
    ItemOccurrenceDialogs(occurrenceDialogState, onOccurrenceEvent)
}

@Composable
private fun ItemDetailTopBarActions(
    itemId: Long?,
    uiState: ItemDetailUiState,
    onEvent: (ItemDetailEvent) -> Unit,
    onNavigateToHistory: (Long) -> Unit,
) {
    ItemActions(
        onShareClicked = { onEvent(ItemDetailEvent.OnShareClicked) },
        onHistoryClicked = if (uiState.isEditing && uiState.recurrence != Recurrence.None && itemId != null) {
            { onNavigateToHistory(itemId) }
        } else {
            null
        },
        onDeleteClicked = if (uiState.isEditing) {
            { onEvent(ItemDetailEvent.OnDeleteClicked) }
        } else {
            null
        },
    )
}

@Composable
private fun ItemDetailDialogs(
    dialogState: ItemDetailDialogState,
    onEvent: (ItemDetailEvent) -> Unit,
) {
    if (dialogState is ItemDetailDialogState.DeleteConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.item_detail_delete_title,
            messageRes = R.string.item_detail_delete_message,
            onDismiss = { onEvent(ItemDetailEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemDetailEvent.OnDeleteConfirmClicked) },
        )
    }

    if (dialogState is ItemDetailDialogState.DiscardConfirm) {
        DeleteConfirmationDialog(
            titleRes = dialogState.titleRes,
            messageRes = dialogState.messageRes,
            onDismiss = { onEvent(ItemDetailEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemDetailEvent.OnDiscardConfirmed) },
        )
    }
}

@Composable
private fun ItemOccurrenceDialogs(
    dialogState: ItemOccurrenceDialogState,
    onEvent: (ItemOccurrenceEvent) -> Unit,
) {
    if (dialogState is ItemOccurrenceDialogState.ReopenConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.item_detail_reopen_title,
            messageRes = R.string.item_detail_reopen_message,
            onDismiss = { onEvent(ItemOccurrenceEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemOccurrenceEvent.OnCompleteConfirmClicked) },
        )
    }

    if (dialogState is ItemOccurrenceDialogState.CompleteConfirm) {
        NoteConfirmDialog(
            titleRes = if (dialogState.isLate) {
                R.string.item_detail_complete_late_confirm_title
            } else {
                R.string.item_detail_complete_confirm_title
            },
            messageRes = if (dialogState.isLate) {
                R.string.item_detail_complete_late_confirm_message
            } else {
                R.string.item_detail_complete_confirm_message
            },
            noteRequired = dialogState.isLate,
            onDismiss = { onEvent(ItemOccurrenceEvent.OnDialogDismissed) },
            onConfirm = { note -> onEvent(ItemOccurrenceEvent.OnCompleteWithNoteConfirmClicked(note)) },
        )
    }

    if (dialogState is ItemOccurrenceDialogState.IgnoreConfirm) {
        NoteConfirmDialog(
            titleRes = R.string.item_detail_ignore_confirm_title,
            messageRes = R.string.item_detail_ignore_confirm_message,
            noteRequired = true,
            onDismiss = { onEvent(ItemOccurrenceEvent.OnDialogDismissed) },
            onConfirm = { note -> onEvent(ItemOccurrenceEvent.OnIgnoreConfirmClicked(note.orEmpty())) },
        )
    }

    if (dialogState is ItemOccurrenceDialogState.ExtendDeadlineConfirm) {
        ExtendDeadlineDatePickerDialog(
            currentDueDate = dialogState.currentDueDate,
            onDismiss = { onEvent(ItemOccurrenceEvent.OnDialogDismissed) },
            onConfirm = { newDueDate ->
                onEvent(ItemOccurrenceEvent.OnExtendDeadlineConfirmClicked(newDueDate))
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun ItemDetailScreenPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) previewState: ItemDetailUiState,
) {
    UdsTheme {
        ItemDetailScreenContent(
            itemId = 1L,
            uiState = previewState,
            dialogState = ItemDetailDialogState.None,
            occurrenceState = ItemOccurrenceUiState(),
            occurrenceDialogState = ItemOccurrenceDialogState.None,
            onEvent = {},
            onOccurrenceEvent = {},
            onNavigateBack = {},
            onNavigateToHistory = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
