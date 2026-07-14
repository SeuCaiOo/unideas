package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.UrgencyLevel
import com.seucaio.unideas.ds.components.legacy.UnideasListItem
import com.seucaio.unideas.ds.components.legacy.UrgencyIndicator
import com.seucaio.unideas.ds.components.legacy.UrgencyIndicatorLevel
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A row in Home's fixed panel or active tab list. Only [ItemType.TASK] gets a completion
 * checkbox; the recurrence icon only shows for items that actually have both a due date and a
 * recurrence (same guard `ItemDetailScreen` uses for its recurrence metadata row).
 */
@Composable
fun HomeItemRow(
    item: Item,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UnideasListItem(
        title = item.title,
        modifier = modifier,
        onClick = onClick,
        leadingContent = if (item.type == ItemType.TASK) {
            { Checkbox(checked = item.isCompleted, onCheckedChange = { onComplete() }) }
        } else {
            null
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.dueDate != null && item.isRecurring) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.home_item_recurring_content_description),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                if (item.dueDate != null) {
                    UrgencyIndicator(level = item.urgency(LocalDate.now(), Constants.DUE_SOON_DAYS).toIndicatorLevel())
                }
            }
        },
    )
}

private fun UrgencyLevel.toIndicatorLevel(): UrgencyIndicatorLevel = when (this) {
    UrgencyLevel.OVERDUE -> UrgencyIndicatorLevel.OVERDUE
    UrgencyLevel.DUE_SOON -> UrgencyIndicatorLevel.DUE_SOON
    UrgencyLevel.NORMAL -> UrgencyIndicatorLevel.NORMAL
}

private val previewItem = Item(
    id = 1L,
    type = ItemType.TASK,
    title = "Pagar contas",
    dueDate = LocalDate.of(2026, 7, 1),
    recurrence = Recurrence.Weekly,
    createdAt = LocalDateTime.of(2026, 6, 20, 10, 0),
)

@PreviewLightDark
@Composable
private fun HomeItemRowPreview() {
    UdsTheme {
        Surface {
            HomeItemRow(item = previewItem, onClick = {}, onComplete = {})
        }
    }
}
