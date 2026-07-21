package com.seucaio.unideas.feature.items.features.detail.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.UrgencyLevel
import com.seucaio.unideas.ds.components.chips.DueBadge
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Mirrors `:uds`'s native `MetaRow` row shape (label left, value right, divider) but swaps the
 * value for [DueBadge] — its dot + colored label is what the legacy `UrgencyIndicator` this
 * replaces conveyed, which `MetaRow`'s plain-text value slot can't express on its own.
 */
@Composable
fun DueDateRow(item: Item, isLast: Boolean, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.item_detail_date_label),
                style = AppType.Metadata,
                color = LocalUdsExtendedColors.current.textTertiary,
                fontSize = 13.sp,
            )
            val dueDate = item.dueDate
            if (dueDate != null) {
                DueBadge(label = dueDate.toFormattedDateString(), color = dueDateUrgencyColor(item))
            } else {
                Text(stringResource(R.string.item_form_date_none), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
        if (!isLast) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }
}

@Composable
private fun dueDateUrgencyColor(item: Item, today: LocalDate = LocalDate.now()): Color =
    when (item.urgency(today, Constants.DUE_SOON_DAYS)) {
        UrgencyLevel.OVERDUE -> MaterialTheme.colorScheme.error
        UrgencyLevel.DUE_SOON -> LocalUdsExtendedColors.current.warning
        UrgencyLevel.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

private val previewCreatedAt = LocalDateTime.of(2026, 6, 20, 10, 0)
private val previewOverdueDate: LocalDate = LocalDate.now().minusDays(OVERDUE_DAYS)

private const val OVERDUE_DAYS = 3L

@PreviewLightDark
@Composable
private fun DueDateRowPreview() {
    UdsTheme {
        Surface {
            Column {
                DueDateRow(
                    item = Item(
                        type = ItemType.TASK,
                        title = "Overdue",
                        dueDate = previewOverdueDate,
                        createdAt = previewCreatedAt,
                    ),
                    isLast = false,
                )
                DueDateRow(
                    item = Item(type = ItemType.TASK, title = "No date", createdAt = previewCreatedAt),
                    isLast = true,
                )
            }
        }
    }
}
