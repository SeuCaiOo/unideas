package com.seucaio.unideas.feature.items.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.core.common.extensions.toFormattedTimeString
import com.seucaio.unideas.domain.model.CompletionStatus
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.temporal.ChronoUnit

@Composable
internal fun ItemHistoryCard(entry: ItemCompletionHistory) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.scheduledDate.toFormattedDateString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = entry.status.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = entry.status.color(),
                )
            }
            ItemHistoryCardDetails(entry)
        }
    }
}

@Composable
private fun ItemHistoryCardDetails(entry: ItemCompletionHistory) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entry.completedAt?.let { completedAt ->
            Text(
                text = stringResource(
                    R.string.item_history_completed_at,
                    completedAt.toFormattedTimeString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        entry.lateDays()?.let { days ->
            Text(
                text = pluralStringResource(R.plurals.item_history_days_late, days, days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        val originalScheduledDate = entry.originalScheduledDate
        if (entry.extensionCount > 0 && originalScheduledDate != null) {
            Text(
                text = stringResource(
                    R.string.item_history_extended_trail,
                    entry.extensionCount,
                    originalScheduledDate.toFormattedDateString(),
                    entry.scheduledDate.toFormattedDateString(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        entry.note?.let { note ->
            Text(
                text = stringResource(R.string.item_detail_history_note_label, note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun ItemCompletionHistory.lateDays(): Int? {
    val completed = completedAt ?: return null
    val days = ChronoUnit.DAYS.between(scheduledDate, completed.toLocalDate()).toInt()
    return days.takeIf { it > 0 }
}

@Composable
private fun CompletionStatus.label(): String = stringResource(
    when (this) {
        CompletionStatus.ON_TIME -> R.string.item_detail_history_status_on_time
        CompletionStatus.LATE -> R.string.item_detail_history_status_late
        CompletionStatus.MISSED -> R.string.item_detail_history_status_missed
    },
)

@Composable
private fun CompletionStatus.color() = when (this) {
    CompletionStatus.ON_TIME -> MaterialTheme.colorScheme.primary
    CompletionStatus.LATE -> MaterialTheme.colorScheme.tertiary
    CompletionStatus.MISSED -> MaterialTheme.colorScheme.error
}

@PreviewLightDark
@Composable
private fun ItemHistoryCardPreview(
    @PreviewParameter(ItemHistoryCardPreviewProvider::class) entry: ItemCompletionHistory,
) {
    UdsTheme {
        Surface {
            ItemHistoryCard(entry)
        }
    }
}
