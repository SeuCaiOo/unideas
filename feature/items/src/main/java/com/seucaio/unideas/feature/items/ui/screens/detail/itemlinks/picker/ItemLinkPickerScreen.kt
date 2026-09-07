package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.item.ListItemRow
import com.seucaio.unideas.ds.components.lists.model.ListItemUi
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel.ItemLinkPickerEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel.ItemLinkPickerUiAction
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel.ItemLinkPickerUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel.ItemLinkPickerViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDateTime

@Composable
fun ItemLinkPickerScreen(
    itemId: Long,
    type: ItemType,
    onNavigateBack: () -> Unit,
    viewModel: ItemLinkPickerViewModel = koinViewModel { parametersOf(itemId, type) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    HandlePickerUiAction(viewModel.uiAction, updatedOnNavigateBack, snackbarHostState)

    ItemLinkPickerScreenContent(
        uiState = uiState,
        type = type,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun HandlePickerUiAction(
    uiAction: Flow<ItemLinkPickerUiAction>,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    LaunchedEffect(Unit) {
        uiAction.collect { action ->
            when (action) {
                is ItemLinkPickerUiAction.NavigateBack -> updatedOnNavigateBack()
                is ItemLinkPickerUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }
}

@Composable
private fun ItemLinkPickerScreenContent(
    uiState: ItemLinkPickerUiState,
    type: ItemType,
    onEvent: (ItemLinkPickerEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val titleRes = if (type == ItemType.TASK) {
        R.string.item_links_picker_title_task
    } else {
        R.string.item_links_picker_title_note
    }

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(titleRes),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEvent(ItemLinkPickerEvent.OnConfirmClicked) }) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.item_links_picker_confirm_content_description),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (uiState) {
            ItemLinkPickerUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
            is ItemLinkPickerUiState.Error -> UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(ItemLinkPickerEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )

            is ItemLinkPickerUiState.Success -> ItemLinkPickerSuccessContent(uiState, padding, onEvent)
        }
    }
}

@Composable
private fun ItemLinkPickerSuccessContent(
    uiState: ItemLinkPickerUiState.Success,
    padding: PaddingValues,
    onEvent: (ItemLinkPickerEvent) -> Unit,
) {
    if (uiState.items.isEmpty()) {
        UnideasEmptyContent(
            messageRes = R.string.item_links_picker_empty,
            modifier = Modifier.padding(padding).fillMaxSize(),
        )
    } else {
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(uiState.items, key = { it.id }) { item ->
                ListItemRow(
                    ui = item.toPickerListItemUi(isSelected = item.id in uiState.selectedIds),
                    onClick = { onEvent(ItemLinkPickerEvent.OnItemToggled(item.id)) },
                    onToggleCheck = { onEvent(ItemLinkPickerEvent.OnItemToggled(item.id)) },
                    onToggleSelection = { onEvent(ItemLinkPickerEvent.OnItemToggled(item.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun Item.toPickerListItemUi(isSelected: Boolean): ListItemUi = ListItemUi(
    id = id,
    title = title,
    meta = null,
    showCheckbox = false,
    checked = false,
    showRepeatIcon = false,
    badgeLabel = null,
    badgeColor = Color.Transparent,
    checkContentDescription = "",
    isSelected = isSelected,
)

private class ItemLinkPickerPreviewProvider : PreviewParameterProvider<ItemLinkPickerUiState> {
    override val values: Sequence<ItemLinkPickerUiState> = sequenceOf(
        ItemLinkPickerUiState.Loading,
        ItemLinkPickerUiState.Success(
            items = listOf(
                Item(id = 1L, type = ItemType.TASK, title = "Revisar PR", createdAt = LocalDateTime.now()),
                Item(id = 2L, type = ItemType.TASK, title = "Pagar conta", createdAt = LocalDateTime.now()),
            ),
            selectedIds = setOf(1L),
        ),
        ItemLinkPickerUiState.Success(),
        ItemLinkPickerUiState.Error(R.string.item_links_load_error),
    )
}

@PreviewLightDark
@Composable
private fun ItemLinkPickerScreenPreview(
    @PreviewParameter(ItemLinkPickerPreviewProvider::class) previewState: ItemLinkPickerUiState,
) {
    UdsTheme {
        ItemLinkPickerScreenContent(
            uiState = previewState,
            type = ItemType.TASK,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
