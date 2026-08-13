package com.seucaio.unideas.feature.items.ui.components.fields.recurrence

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.nextOrSame
import com.seucaio.unideas.core.common.extensions.nextOrSameDayOfMonth
import com.seucaio.unideas.core.common.extensions.orToday
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun RecurrenceField(
    recurrence: Recurrence,
    dueDate: LocalDate?,
    onRecurrenceChanged: (Recurrence) -> Unit,
    onDueDateChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetStep: RecurrenceSheetStep by remember { mutableStateOf(RecurrenceSheetStep.None) }

    DateFieldButton(
        valueLabel = recurrence.label(dueDate),
        defaultValue = stringResource(R.string.item_form_recurrence_none),
        onClick = { sheetStep = RecurrenceSheetStep.List },
        onClear = { onRecurrenceChanged(Recurrence.None) },
        clearContentDescription = stringResource(R.string.item_form_recurrence_clear),
        modifier = modifier,
    )

    when (val step = sheetStep) {
        RecurrenceSheetStep.None -> Unit
        RecurrenceSheetStep.List -> RecurrenceBottomSheet(
            recurrence = recurrence,
            onRecurrenceSelected = {
                sheetStep = when {
                    it is Recurrence.EveryNDays -> RecurrenceSheetStep.EveryNDaysStepper(it.days)
                    it == Recurrence.Weekly ->
                        RecurrenceSheetStep.WeekdayPicker(dueDate.orToday().dayOfWeek)
                    it == Recurrence.Monthly ->
                        RecurrenceSheetStep.DayOfMonthPicker(dueDate.orToday().dayOfMonth)
                    else -> {
                        onRecurrenceChanged(it)
                        RecurrenceSheetStep.None
                    }
                }
            },
            onDismiss = { sheetStep = RecurrenceSheetStep.None },
        )
        is RecurrenceSheetStep.EveryNDaysStepper -> EveryNDaysBottomSheet(
            days = step.days,
            onDaysConfirmed = {
                onRecurrenceChanged(Recurrence.EveryNDays(it))
                sheetStep = RecurrenceSheetStep.None
            },
            onDismiss = { sheetStep = RecurrenceSheetStep.None },
        )
        is RecurrenceSheetStep.WeekdayPicker -> WeekdayBottomSheet(
            selectedDay = step.day,
            onDaySelected = {
                onRecurrenceChanged(Recurrence.Weekly)
                onDueDateChanged(LocalDate.now().nextOrSame(it))
                sheetStep = RecurrenceSheetStep.None
            },
            onDismiss = { sheetStep = RecurrenceSheetStep.None },
        )
        is RecurrenceSheetStep.DayOfMonthPicker -> DayOfMonthBottomSheet(
            selectedDay = step.day,
            onDaySelected = {
                onRecurrenceChanged(Recurrence.Monthly)
                onDueDateChanged(LocalDate.now().nextOrSameDayOfMonth(it))
                sheetStep = RecurrenceSheetStep.None
            },
            onDismiss = { sheetStep = RecurrenceSheetStep.None },
        )
    }
}

private sealed interface RecurrenceSheetStep {
    data object None : RecurrenceSheetStep
    data object List : RecurrenceSheetStep
    data class EveryNDaysStepper(val days: Int) : RecurrenceSheetStep
    data class WeekdayPicker(val day: DayOfWeek) : RecurrenceSheetStep
    data class DayOfMonthPicker(val day: Int) : RecurrenceSheetStep
}

@Composable
internal fun Recurrence.label(dueDate: LocalDate? = null): String? = when (this) {
    Recurrence.None -> null
    Recurrence.Daily -> stringResource(R.string.item_form_recurrence_daily)
    Recurrence.Weekly -> if (dueDate != null) {
        stringResource(R.string.item_form_recurrence_weekly_with_day, dueDate.dayOfWeek.label())
    } else {
        stringResource(R.string.item_form_recurrence_weekly)
    }
    Recurrence.Monthly -> if (dueDate != null) {
        stringResource(R.string.item_form_recurrence_monthly_with_day, dueDate.dayOfMonth)
    } else {
        stringResource(R.string.item_form_recurrence_monthly)
    }
    is Recurrence.EveryNDays -> stringResource(R.string.item_form_recurrence_every_n_days, days)
}

private val PREVIEW_DUE_DATE = LocalDate.of(2026, 8, 20)

private class RecurrenceFieldPreviewProvider : PreviewParameterProvider<Recurrence> {
    override val values = sequenceOf(
        Recurrence.None,
        Recurrence.Daily,
        Recurrence.Weekly,
        Recurrence.Monthly,
        Recurrence.EveryNDays(3),
    )
}

@PreviewLightDark
@Composable
private fun RecurrenceFieldPreview(
    @PreviewParameter(RecurrenceFieldPreviewProvider::class) recurrence: Recurrence,
) {
    UdsTheme {
        Surface {
            RecurrenceField(
                recurrence = recurrence,
                dueDate = PREVIEW_DUE_DATE,
                onRecurrenceChanged = {},
                onDueDateChanged = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
