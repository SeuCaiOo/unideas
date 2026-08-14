package com.seucaio.unideas.feature.items.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.ds.components.chips.SelectableChipRow
import com.seucaio.unideas.ds.components.chips.SelectableChipUi
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.HistoryFilter
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryEvent
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryUiState
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ItemHistoryScreen(
    itemId: Long,
    onNavigateBack: (() -> Unit)?,
    viewModel: ItemHistoryViewModel = koinViewModel { parametersOf(itemId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ItemHistoryScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemHistoryScreenContent(
    uiState: ItemHistoryUiState,
    onEvent: (ItemHistoryEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.item_detail_history_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        ItemHistoryContent(uiState = uiState, onEvent = onEvent, contentPadding = padding)
    }
}

@Composable
internal fun ItemHistoryContent(
    uiState: ItemHistoryUiState,
    onEvent: (ItemHistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { HistorySummaryCard(uiState) }
        item { HistoryFilterRow(uiState.activeFilter, onEvent) }

        if (uiState.filteredHistory.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.item_detail_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(uiState.filteredHistory) { entry -> ItemHistoryCard(entry) }
        }
    }
}

@Composable
private fun HistorySummaryCard(uiState: ItemHistoryUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${uiState.onTimeRatePercent}%",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = " " + stringResource(R.string.item_history_rate_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = pluralStringResource(
                    R.plurals.item_history_occurrence_count,
                    uiState.history.size,
                    uiState.history.size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HistoryCountChip(
                    uiState.onTimeCount,
                    R.string.item_history_filter_on_time,
                    MaterialTheme.colorScheme.primary
                )
                HistoryCountChip(
                    uiState.lateCount,
                    R.string.item_detail_history_status_late,
                    MaterialTheme.colorScheme.tertiary
                )
                HistoryCountChip(
                    uiState.missedCount,
                    R.string.item_detail_history_status_missed,
                    MaterialTheme.colorScheme.error
                )
            }

            if (uiState.currentStreak > 0) {
                Text(
                    text = stringResource(R.string.item_history_streak, uiState.currentStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun HistoryCountChip(count: Int, labelRes: Int, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun HistoryFilterRow(activeFilter: HistoryFilter, onEvent: (ItemHistoryEvent) -> Unit) {
    val chips = listOf(
        HistoryFilter.ALL to R.string.item_history_filter_all,
        HistoryFilter.ON_TIME to R.string.item_history_filter_on_time,
        HistoryFilter.LATE to R.string.item_history_filter_late,
        HistoryFilter.WITH_NOTE to R.string.item_history_filter_with_note,
    )
    SelectableChipRow(
        chips = chips.map { (filter, labelRes) ->
            SelectableChipUi(
                id = filter.name,
                label = stringResource(labelRes),
                selected = filter == activeFilter,
            )
        },
        onToggle = { id -> onEvent(ItemHistoryEvent.OnFilterSelected(HistoryFilter.valueOf(id))) },
    )
}

@PreviewLightDark
@Composable
private fun ItemHistoryContentPreview(
    @PreviewParameter(ItemHistoryContentPreviewProvider::class) uiState: ItemHistoryUiState,
) {
    UdsTheme {
        Surface {
            ItemHistoryContent(uiState = uiState, onEvent = {})
        }
    }
}
