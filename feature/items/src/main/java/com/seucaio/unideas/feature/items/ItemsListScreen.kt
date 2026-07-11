package com.seucaio.unideas.feature.items

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.ui.components.UnideasEmptyContent
import com.seucaio.unideas.core.ui.components.UnideasErrorContent
import com.seucaio.unideas.core.ui.components.UnideasListItem
import com.seucaio.unideas.core.ui.components.UnideasLoadingContent
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.viewmodel.ItemsListEvent
import com.seucaio.unideas.feature.items.viewmodel.ItemsListUiAction
import com.seucaio.unideas.feature.items.viewmodel.ItemsListUiState
import com.seucaio.unideas.feature.items.viewmodel.ItemsListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ItemsListScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: () -> Unit,
    viewModel: ItemsListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToForm by rememberUpdatedState(onNavigateToForm)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemsListUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is ItemsListUiAction.NavigateToForm -> updatedOnNavigateToForm()
            }
        }
    }

    ItemsListContent(uiState = uiState, onEvent = viewModel::onEvent, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsListContent(
    uiState: ItemsListUiState,
    onEvent: (ItemsListEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(title = stringResource(R.string.items_list_title), onNavigateBack = updatedOnNavigateBack)
        },
        floatingActionButton = {
            // FAB only once we have a definitive answer (empty or with data) — not while
            // loading or errored, same convention as SectionsScreen/TagsScreen.
            if (uiState is ItemsListUiState.Success) {
                FloatingActionButton(onClick = { onEvent(ItemsListEvent.OnAddClicked) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.items_list_add))
                }
            }
        },
    ) { padding ->
        when (uiState) {
            is ItemsListUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
            is ItemsListUiState.Error ->
                UnideasErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onEvent(ItemsListEvent.OnRetryClicked) },
                    modifier = Modifier.padding(padding),
                )
            is ItemsListUiState.Success -> {
                if (uiState.items.isEmpty()) {
                    UnideasEmptyContent(messageRes = R.string.items_list_empty, modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                        items(uiState.items, key = { it.id }) { item ->
                            ItemRow(item = item, onEvent = onEvent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item, onEvent: (ItemsListEvent) -> Unit) {
    val typeRes = if (item.type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note
    UnideasListItem(
        title = item.title,
        subtitle = stringResource(typeRes),
        onClick = { onEvent(ItemsListEvent.OnItemClicked(item.id)) },
    )
}

@PreviewLightDark
@Composable
private fun ItemsListScreenPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) uiState: ItemsListUiState,
) {
    UnideasTheme {
        ItemsListContent(uiState = uiState, onEvent = {}, onNavigateBack = {})
    }
}
