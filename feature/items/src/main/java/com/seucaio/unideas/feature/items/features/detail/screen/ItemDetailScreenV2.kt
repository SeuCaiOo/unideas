package com.seucaio.unideas.feature.items.features.detail.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.extensions.shareText
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.chips.TextBadge
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.MetaChipsRow
import com.seucaio.unideas.ds.components.lists.MetaRow
import com.seucaio.unideas.ds.components.lists.TitleSubtitle
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.detail.screen.components.DueDateRowV2
import com.seucaio.unideas.feature.items.features.detail.screen.components.ItemDetailActionsV2
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailDialogState
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailEvent
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailUiAction
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * V2 (#84): same [ItemDetailViewModel]/[ItemDetailUiState]/[ItemDetailEvent] contract as
 * [ItemDetailScreen] — visual pass only. Swaps the legacy metadata `Row`s/`TagChipRow`/
 * `UrgencyIndicator` for `:uds` native [TitleSubtitle] (title + optional description, any
 * list-backed entity's detail header), [MetaRow], [MetaChipsRow] (read-only tag display) and
 * [TextBadge] (item type), plus [DueDateRowV2] — a feature-local composable mirroring `MetaRow`'s
 * shape but slotting in `:uds`'s native `DueBadge` for the urgency dot+color `MetaRow`'s
 * plain-text value can't express. `UnideasTopBar`, `UnideasLoadingContent`/`UnideasErrorContent`
 * and `DeleteConfirmationDialog` stay as-is — no native equivalent yet.
 */
@Composable
fun ItemDetailScreenV2(
    itemId: Long,
    onNavigateBack: (() -> Unit)?,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: ItemDetailViewModel = koinViewModel { parametersOf(itemId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val updatedOnNavigateToEdit by rememberUpdatedState(onNavigateToEdit)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemDetailUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is ItemDetailUiAction.NavigateToEdit -> updatedOnNavigateToEdit(action.itemId)
                is ItemDetailUiAction.ShareText -> context.shareText(action.text)
                is ItemDetailUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ItemDetailContentV2(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun ItemDetailContentV2(
    uiState: ItemDetailUiState,
    dialogState: ItemDetailDialogState,
    onEvent: (ItemDetailEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = (uiState as? ItemDetailUiState.Success)?.item?.title.orEmpty(),
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    if (uiState is ItemDetailUiState.Success) {
                        ItemDetailActionsV2(
                            uiState.item,
                            onEvent
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (uiState) {
            is ItemDetailUiState.Loading -> UnideasLoadingContent(
                modifier = Modifier.padding(
                    padding
                )
            )

            is ItemDetailUiState.Error ->
                UnideasErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onEvent(ItemDetailEvent.OnRetryClicked) },
                    modifier = Modifier.padding(padding),
                )

            is ItemDetailUiState.Success ->
                ItemDetailBodyV2(state = uiState, modifier = Modifier.padding(padding))
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

@Composable
private fun ItemDetailBodyV2(state: ItemDetailUiState.Success, modifier: Modifier = Modifier) {
    val item = state.item
    val showRecurrence = item.dueDate != null && item.isRecurring

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TitleSubtitle(
            title = item.title,
            subtitle = item.description,
            modifier = Modifier.padding(16.dp)
        )

        val typeRes =
            if (item.type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note
        TextBadge(
            text = stringResource(typeRes),
            background = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        )

        MetaRow(
            label = stringResource(R.string.item_detail_section_label),
            value = state.sectionName ?: stringResource(R.string.item_form_section_none),
        )

        if (item.tags.isNotEmpty()) {
            MetaChipsRow(
                label = stringResource(R.string.item_detail_tags_label),
                chips = item.tags.map { it.name }
            )
        }

        DueDateRowV2(item = item, isLast = !showRecurrence)

        if (showRecurrence) {
            MetaRow(
                label = stringResource(R.string.item_detail_recurrence_label),
                value = recurrenceLabelV2(item.recurrence),
                isLast = true,
            )
        }

        if (item.isCompleted) {
            Text(
                text = stringResource(R.string.item_detail_completed_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun recurrenceLabelV2(recurrence: Recurrence): String = when (recurrence) {
    Recurrence.Daily -> stringResource(R.string.item_form_recurrence_daily)
    Recurrence.Weekly -> stringResource(R.string.item_form_recurrence_weekly)
    Recurrence.Monthly -> stringResource(R.string.item_form_recurrence_monthly)
    else -> stringResource(R.string.item_form_recurrence_none)
}

@PreviewLightDark
@Composable
private fun ItemDetailScreenV2Preview(
    @PreviewParameter(ItemDetailPreviewProvider::class) uiState: ItemDetailUiState,
) {
    UdsTheme {
        ItemDetailContentV2(
            uiState = uiState,
            dialogState = ItemDetailDialogState.None,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
