package com.seucaio.unideas.feature.items.features.detail.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.core.ui.components.DeleteConfirmationDialog
import com.seucaio.unideas.core.ui.components.TagChip
import com.seucaio.unideas.core.ui.components.UnideasErrorContent
import com.seucaio.unideas.core.ui.components.UnideasLoadingContent
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.components.UrgencyIndicator
import com.seucaio.unideas.core.ui.components.UrgencyIndicatorLevel
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.UrgencyLevel
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailDialogState
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailEvent
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailUiAction
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

@Composable
fun ItemDetailScreen(
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
                is ItemDetailUiAction.ShareText -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, action.text),
                        null,
                    ),
                )
                is ItemDetailUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ItemDetailContent(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun ItemDetailContent(
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
                actions = { if (uiState is ItemDetailUiState.Success) ItemDetailActions(uiState.item, onEvent) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (uiState) {
            is ItemDetailUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
            is ItemDetailUiState.Error ->
                UnideasErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onEvent(ItemDetailEvent.OnRetryClicked) },
                    modifier = Modifier.padding(padding),
                )
            is ItemDetailUiState.Success ->
                ItemDetailBody(state = uiState, modifier = Modifier.padding(padding))
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
private fun ItemDetailActions(item: Item, onEvent: (ItemDetailEvent) -> Unit) {
    Row {
        IconButton(onClick = { onEvent(ItemDetailEvent.OnShareClicked) }) {
            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.item_detail_share))
        }
        IconButton(onClick = { onEvent(ItemDetailEvent.OnEditClicked) }) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.item_detail_edit))
        }
        IconButton(onClick = { onEvent(ItemDetailEvent.OnDeleteClicked) }) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.item_detail_delete))
        }
        if (item.type == ItemType.TASK && !item.isCompleted) {
            IconButton(onClick = { onEvent(ItemDetailEvent.OnCompleteClicked) }) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.item_detail_complete))
            }
        }
    }
}

@Composable
private fun ItemDetailBody(state: ItemDetailUiState.Success, modifier: Modifier = Modifier) {
    val item = state.item

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SelectionContainer {
            Column {
                Text(text = item.title, style = MaterialTheme.typography.titleLarge)
                item.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        val typeRes = if (item.type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note
        Text(text = stringResource(typeRes), modifier = Modifier.padding(top = 16.dp))

        MetadataRow(
            label = stringResource(R.string.item_detail_section_label),
            value = state.sectionName ?: stringResource(R.string.item_form_section_none),
        )

        if (item.tags.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(text = stringResource(R.string.item_detail_tags_label))
                LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                    items(item.tags, key = { it.id }) { tag ->
                        TagChip(label = tag.name, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }
        }

        DateAndUrgencyRow(item)

        if (item.dueDate != null && item.isRecurring) {
            MetadataRow(
                label = stringResource(R.string.item_detail_recurrence_label),
                value = recurrenceLabel(item),
            )
        }

        if (item.isCompleted) {
            Text(
                text = stringResource(R.string.item_detail_completed_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun DateAndUrgencyRow(item: Item) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(text = stringResource(R.string.item_detail_date_label), style = MaterialTheme.typography.labelLarge)
        Text(
            text = item.dueDate?.toFormattedDateString() ?: stringResource(R.string.item_form_date_none),
            modifier = Modifier.padding(start = 8.dp),
        )
        if (item.dueDate != null) {
            UrgencyIndicator(
                level = item.urgency(LocalDate.now(), Constants.DUE_SOON_DAYS).toIndicatorLevel(),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private fun UrgencyLevel.toIndicatorLevel(): UrgencyIndicatorLevel = when (this) {
    UrgencyLevel.OVERDUE -> UrgencyIndicatorLevel.OVERDUE
    UrgencyLevel.DUE_SOON -> UrgencyIndicatorLevel.DUE_SOON
    UrgencyLevel.NORMAL -> UrgencyIndicatorLevel.NORMAL
}

@Composable
private fun recurrenceLabel(item: Item): String = when (item.recurrence) {
    Recurrence.Daily -> stringResource(R.string.item_form_recurrence_daily)
    Recurrence.Weekly -> stringResource(R.string.item_form_recurrence_weekly)
    Recurrence.Monthly -> stringResource(R.string.item_form_recurrence_monthly)
    else -> stringResource(R.string.item_form_recurrence_none)
}

@PreviewLightDark
@Composable
private fun ItemDetailScreenPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) uiState: ItemDetailUiState,
) {
    UnideasTheme {
        ItemDetailContent(
            uiState = uiState,
            dialogState = ItemDetailDialogState.None,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
