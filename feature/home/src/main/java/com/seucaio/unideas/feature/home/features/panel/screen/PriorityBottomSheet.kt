package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.ds.components.panels.PriorityPanel
import com.seucaio.unideas.ds.components.panels.PriorityRowUi
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.components.dueBadgeColor
import com.seucaio.unideas.feature.home.features.panel.screen.components.dueBadgeLabel
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import com.seucaio.unideas.ds.R as DsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HomeUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is HomeUiAction.NavigateToAllPriorities -> updatedOnNavigateToAllPriorities()

                is HomeUiAction.NavigateToAddItem,
                is HomeUiAction.NavigateToSettings,
                is HomeUiAction.ShowError -> Unit
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        PriorityBottomSheetContent(
            uiState = uiState,
            itemsState = itemsState,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun PriorityBottomSheetContent(
    uiState: HomeUiState,
    itemsState: HomeItemsState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is HomeUiState.Loading ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

        is HomeUiState.Error ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(DsR.string.error_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(uiState.messageRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { onEvent(HomeEvent.OnRetryClicked) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(DsR.string.error_action_retry))
                }
            }

        is HomeUiState.Success ->
            Column(modifier = modifier.fillMaxWidth()) {
                PriorityPanel(
                    title = stringResource(R.string.home_panel_title),
                    icon = Icons.Outlined.Flag,
                    rows = itemsState.priorityItems.map { it.toPriorityRowUi() },
                    footerLabel = if (itemsState.showSeeAllButton) stringResource(R.string.home_see_all) else null,
                    onFooterClick = { onEvent(HomeEvent.OnSeeAllClicked) },
                    onRowClick = { id -> onEvent(HomeEvent.OnItemClicked(id)) },
                    emptyText = stringResource(R.string.home_panel_empty),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
    }
}

@Composable
private fun Item.toPriorityRowUi(): PriorityRowUi = PriorityRowUi(
    id = id,
    title = title,
    badgeLabel = dueBadgeLabel(this),
    badgeColor = dueBadgeColor(this),
)

@PreviewLightDark
@Composable
private fun PriorityBottomSheetContentPreview(
    @PreviewParameter(HomePreviewProvider::class) fixture: HomePreviewFixture,
) {
    UdsTheme {
        Surface {
            PriorityBottomSheetContent(
                uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem),
                itemsState = fixture.itemsState,
                onEvent = {},
            )
        }
    }
}
