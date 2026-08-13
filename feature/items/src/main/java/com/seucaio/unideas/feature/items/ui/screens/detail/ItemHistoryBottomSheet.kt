package com.seucaio.unideas.feature.items.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.CompletionStatus
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemHistoryBottomSheet(history: List<ItemCompletionHistory>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        ItemHistorySheetContent(history = history)
    }
}

@Composable
private fun ItemHistorySheetContent(history: List<ItemCompletionHistory>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.item_detail_history_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.item_detail_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(history) { entry -> ItemHistoryRow(entry) }
            }
        }
    }
}

@Composable
private fun ItemHistoryRow(entry: ItemCompletionHistory, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = entry.scheduledDate.toFormattedDateString(), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(entry.status.labelRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = entry.status.color(),
        )
    }
}

private fun CompletionStatus.labelRes(): Int = when (this) {
    CompletionStatus.ON_TIME -> R.string.item_detail_history_status_on_time
    CompletionStatus.LATE -> R.string.item_detail_history_status_late
    CompletionStatus.MISSED -> R.string.item_detail_history_status_missed
}

@Composable
private fun CompletionStatus.color() = when (this) {
    CompletionStatus.ON_TIME -> MaterialTheme.colorScheme.primary
    CompletionStatus.LATE -> MaterialTheme.colorScheme.tertiary
    CompletionStatus.MISSED -> MaterialTheme.colorScheme.error
}

@PreviewLightDark
@Composable
private fun ItemHistorySheetContentPreview() {
    UdsTheme {
        Surface {
            ItemHistorySheetContent(
                history = listOf(
                    ItemCompletionHistory(
                        id = 1L,
                        itemId = 1L,
                        scheduledDate = LocalDate.of(2026, 7, 1),
                        completedAt = LocalDateTime.of(2026, 7, 1, 10, 0),
                    ),
                    ItemCompletionHistory(
                        id = 2L,
                        itemId = 1L,
                        scheduledDate = LocalDate.of(2026, 7, 8),
                        completedAt = LocalDateTime.of(2026, 7, 9, 10, 0),
                    ),
                    ItemCompletionHistory(
                        id = 3L,
                        itemId = 1L,
                        scheduledDate = LocalDate.of(2026, 7, 15),
                        completedAt = null,
                    ),
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemHistorySheetContentEmptyPreview() {
    UdsTheme {
        Surface {
            ItemHistorySheetContent(history = emptyList())
        }
    }
}
