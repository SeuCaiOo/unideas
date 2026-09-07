package com.seucaio.unideas.feature.home.features.hiddenitems.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.item.ListItemRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel.HiddenItemsEvent
import com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel.HiddenItemsUiState
import com.seucaio.unideas.feature.home.features.home.screen.components.items.toListItemUi

@Composable
internal fun HiddenItemsListContent(
    uiState: HiddenItemsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: (() -> Unit)?,
    onEvent: (HiddenItemsEvent) -> Unit,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.hidden_items_title),
                onNavigateBack = updatedOnNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (uiState) {
            is HiddenItemsUiState.Loading ->
                UnideasLoadingContent(modifier = Modifier.padding(padding))

            is HiddenItemsUiState.Error ->
                UnideasErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onEvent(HiddenItemsEvent.OnRetryClicked) },
                    modifier = Modifier.padding(padding),
                )

            is HiddenItemsUiState.Success ->
                HiddenItemsSuccessContent(
                    items = uiState.items,
                    padding = padding,
                    onEvent = onEvent,
                )
        }
    }
}

@Composable
private fun HiddenItemsSuccessContent(
    items: List<Item>,
    padding: PaddingValues,
    onEvent: (HiddenItemsEvent) -> Unit,
) {
    if (items.isEmpty()) {
        UnideasEmptyContent(
            messageRes = R.string.hidden_items_empty,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        )
    } else {
        val checkContentDescription =
            stringResource(R.string.home_item_recurring_content_description)
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                ListItemRow(
                    ui = item.toListItemUi(checkContentDescription),
                    onClick = { onEvent(HiddenItemsEvent.OnItemClicked(item.id)) },
                    onToggleCheck = { onEvent(HiddenItemsEvent.OnItemClicked(item.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HiddenItemsListContentPreview(
    @PreviewParameter(HiddenItemsPreviewProvider::class) uiState: HiddenItemsUiState,
) {
    UdsTheme {
        HiddenItemsListContent(
            uiState = uiState,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onEvent = {},
        )
    }
}
