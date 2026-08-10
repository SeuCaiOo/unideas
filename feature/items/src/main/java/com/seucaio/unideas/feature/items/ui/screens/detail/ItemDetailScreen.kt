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
import com.seucaio.unideas.domain.model.ItemCompletionHistory
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
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailDialogState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiAction
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel
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
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemDetailViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
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
            }
        }
    }

    ItemDetailScreenContent(
        uiState = uiState,
        dialogState = dialogState,
        historyState = historyState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailScreenContent(
    uiState: ItemDetailUiState,
    dialogState: ItemDetailDialogState,
    historyState: List<ItemCompletionHistory>,
    onEvent: (ItemDetailEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    BackHandler {
        onEvent(ItemDetailEvent.OnBackRequested)
    }

    val topBarNavigateBack = updatedOnNavigateBack?.let {
        { onEvent(ItemDetailEvent.OnBackRequested) }
    }

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
            onSaveClicked = { onEvent(ItemDetailEvent.OnSaveClicked) },
            onCompleteClicked = { onEvent(ItemDetailEvent.OnCompleteClicked) },
        )
    }

    Scaffold(
        topBar = {
            UnideasTopBar(
                onNavigateBack = topBarNavigateBack,
                actions = {
                    ItemActions(
                        onShareClicked = { onEvent(ItemDetailEvent.OnShareClicked) },
                        onDeleteClicked = { onEvent(ItemDetailEvent.OnDeleteClicked) },
                        onHistoryClicked = if (uiState.isEditing && uiState.recurrence != Recurrence.None) {
                            { onEvent(ItemDetailEvent.OnHistoryClicked) }
                        } else {
                            null
                        },
                    )
                },
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
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (dialogState is ItemDetailDialogState.DeleteConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.item_detail_delete_title,
            messageRes = R.string.item_detail_delete_message,
            onDismiss = { onEvent(ItemDetailEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemDetailEvent.OnDeleteConfirmClicked) },
        )
    }

    if (dialogState is ItemDetailDialogState.ReopenConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.item_detail_reopen_title,
            messageRes = R.string.item_detail_reopen_message,
            onDismiss = { onEvent(ItemDetailEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemDetailEvent.OnCompleteConfirmClicked) },
        )
    }

    if (dialogState is ItemDetailDialogState.History) {
        ItemHistoryBottomSheet(
            history = historyState,
            onDismiss = { onEvent(ItemDetailEvent.OnDialogDismissed) },
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

@PreviewLightDark
@Composable
private fun ItemDetailScreenPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) previewState: ItemDetailUiState,
) {
    UdsTheme {
        ItemDetailScreenContent(
            uiState = previewState,
            dialogState = ItemDetailDialogState.None,
            historyState = emptyList(),
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
