package com.seucaio.unideas.feature.home.features.priority.screen

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
import com.seucaio.unideas.feature.home.features.home.screen.components.dueBadgeColor
import com.seucaio.unideas.feature.home.features.home.screen.components.dueBadgeLabel
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityEvent
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityItemsState
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityUiAction
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityUiState
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityViewModel
import org.koin.androidx.compose.koinViewModel
import com.seucaio.unideas.ds.R as DsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    viewModel: PriorityViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is PriorityUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is PriorityUiAction.NavigateToAllPriorities -> updatedOnNavigateToAllPriorities()
                is PriorityUiAction.ShowError -> Unit
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
    uiState: PriorityUiState,
    itemsState: PriorityItemsState,
    onEvent: (PriorityEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PriorityUiState.Loading ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

        is PriorityUiState.Error ->
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
                    onClick = { onEvent(PriorityEvent.OnRetryClicked) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(DsR.string.error_action_retry))
                }
            }

        is PriorityUiState.Success ->
            Column(modifier = modifier.fillMaxWidth()) {
                PriorityPanel(
                    title = stringResource(R.string.home_panel_title),
                    icon = Icons.Outlined.Flag,
                    rows = itemsState.priorityItems.map { it.toPriorityRowUi() },
                    footerLabel = if (itemsState.showSeeAllButton) stringResource(R.string.home_see_all) else null,
                    onFooterClick = { onEvent(PriorityEvent.OnSeeAllClicked) },
                    onRowClick = { id -> onEvent(PriorityEvent.OnItemClicked(id)) },
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
    @PreviewParameter(PriorityPreviewProvider::class) fixture: PriorityPreviewFixture,
) {
    UdsTheme {
        Surface {
            PriorityBottomSheetContent(
                uiState = PriorityUiState.Success(hasAnyItem = fixture.hasAnyItem),
                itemsState = fixture.itemsState,
                onEvent = {},
            )
        }
    }
}
