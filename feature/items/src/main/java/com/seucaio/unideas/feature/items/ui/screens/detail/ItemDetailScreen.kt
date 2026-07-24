package com.seucaio.unideas.feature.items.ui.screens.detail

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
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.ItemActions
import com.seucaio.unideas.feature.items.ui.components.ItemFormBody
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailDialogState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiAction
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Full-screen destination for viewing/editing an existing item (#86/#97) — reached via
 * `ItemsRoute.Detail`. Always editable, no separate read-only state: [ItemActions]
 * (share/delete/complete) lives in the top bar instead of a distinct "editar" affordance.
 * Creating a new item is a separate flow ([AddItemSheet], the bottom sheet reached via
 * `ItemsRoute.AddItem`) — this screen is edit-only in practice, though `itemId` stays nullable
 * to keep [ItemDetailViewModel] reusable for either case.
 */
@Composable
fun ItemDetailScreen(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemDetailViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
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
                is ItemDetailUiAction.ShareText -> context.shareText(action.text)
            }
        }
    }

    ItemDetailScreenContent(
        uiState = uiState,
        dialogState = dialogState,
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
    onEvent: (ItemDetailEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val fieldsEvents = remember(onEvent) {
        ItemFormFieldsEvents(
            onTypeChanged = { onEvent(ItemDetailEvent.OnTypeChanged(it)) },
            onTitleChanged = { onEvent(ItemDetailEvent.OnTitleChanged(it)) },
            onDescriptionChanged = { onEvent(ItemDetailEvent.OnDescriptionChanged(it)) },
            onSectionChanged = { onEvent(ItemDetailEvent.OnSectionChanged(it)) },
            onTagToggled = { onEvent(ItemDetailEvent.OnTagToggled(it)) },
            onDueDateChanged = { onEvent(ItemDetailEvent.OnDueDateChanged(it)) },
            onRecurrenceChanged = { onEvent(ItemDetailEvent.OnRecurrenceChanged(it)) },
            onSaveClicked = { onEvent(ItemDetailEvent.OnSaveClicked) },
        )
    }

    Scaffold(
        topBar = {
            UnideasTopBar(
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    ItemActions(
                        canComplete = uiState.typeIsTask && !uiState.isCompleted,
                        onShareClicked = { onEvent(ItemDetailEvent.OnShareClicked) },
                        onDeleteClicked = { onEvent(ItemDetailEvent.OnDeleteClicked) }
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
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
