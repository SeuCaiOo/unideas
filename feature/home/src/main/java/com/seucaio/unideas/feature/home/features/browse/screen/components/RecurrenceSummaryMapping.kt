package com.seucaio.unideas.feature.home.features.browse.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.feature.home.R
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
internal fun Recurrence.summaryLabel(dueDate: LocalDate?): String? = when (this) {
    Recurrence.None -> null
    Recurrence.Daily -> stringResource(R.string.home_recurrence_daily)
    Recurrence.Weekly -> if (dueDate != null) {
        val locale = LocalConfiguration.current.locales[0]
        stringResource(
            R.string.home_recurrence_weekly_with_day,
            dueDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        )
    } else {
        stringResource(R.string.home_recurrence_weekly)
    }
    Recurrence.Monthly -> if (dueDate != null) {
        stringResource(R.string.home_recurrence_monthly_with_day, dueDate.dayOfMonth)
    } else {
        stringResource(R.string.home_recurrence_monthly)
    }
    is Recurrence.EveryNDays -> stringResource(R.string.home_recurrence_every_n_days, days)
}
