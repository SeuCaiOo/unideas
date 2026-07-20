package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.UrgencyLevel
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.feature.home.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * V2-only (#84): `:uds`'s native `ListItemRow`/`PriorityPanel` take a pre-formatted
 * `badgeLabel: String?` + `badgeColor: Color` — this derives both from [Item.dueDate], purely
 * presentational, no new domain data.
 */
@Composable
internal fun dueBadgeLabel(item: Item, today: LocalDate = LocalDate.now()): String? {
    val dueDate = item.dueDate ?: return null
    val days = ChronoUnit.DAYS.between(today, dueDate).toInt()
    return when {
        days < 0 -> pluralStringResource(R.plurals.home_overdue_days, -days, -days)
        days == 0 -> stringResource(R.string.home_due_today)
        else -> pluralStringResource(R.plurals.home_due_in_days, days, days)
    }
}

@Composable
internal fun dueBadgeColor(item: Item, today: LocalDate = LocalDate.now()): Color =
    when (item.urgency(today, Constants.DUE_SOON_DAYS)) {
        UrgencyLevel.OVERDUE -> MaterialTheme.colorScheme.error
        UrgencyLevel.DUE_SOON -> LocalUdsExtendedColors.current.warning
        UrgencyLevel.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
