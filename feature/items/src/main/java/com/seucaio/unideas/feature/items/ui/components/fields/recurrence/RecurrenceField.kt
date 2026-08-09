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
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun RecurrenceField(
    recurrence: Recurrence,
    onRecurrenceChanged: (Recurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetStep: RecurrenceSheetStep by remember { mutableStateOf(RecurrenceSheetStep.None) }

    DateFieldButton(
        valueLabel = recurrence.label(),
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
                sheetStep = if (it is Recurrence.EveryNDays) {
                    RecurrenceSheetStep.EveryNDaysStepper(it.days)
                } else {
                    onRecurrenceChanged(it)
                    RecurrenceSheetStep.None
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
    }
}

private sealed interface RecurrenceSheetStep {
    data object None : RecurrenceSheetStep
    data object List : RecurrenceSheetStep
    data class EveryNDaysStepper(val days: Int) : RecurrenceSheetStep
}

@Composable
internal fun Recurrence.label(): String? = when (this) {
    Recurrence.None -> null
    Recurrence.Daily -> stringResource(R.string.item_form_recurrence_daily)
    Recurrence.Weekly -> stringResource(R.string.item_form_recurrence_weekly)
    Recurrence.Monthly -> stringResource(R.string.item_form_recurrence_monthly)
    is Recurrence.EveryNDays -> stringResource(R.string.item_form_recurrence_every_n_days, days)
}

private sealed interface RecurrenceFieldPreviewScenario {
    data object EmptyField : RecurrenceFieldPreviewScenario
    data object FilledField : RecurrenceFieldPreviewScenario
}

private class RecurrenceFieldPreviewProvider : PreviewParameterProvider<RecurrenceFieldPreviewScenario> {
    override val values = sequenceOf(
        RecurrenceFieldPreviewScenario.EmptyField,
        RecurrenceFieldPreviewScenario.FilledField,
    )
}

@PreviewLightDark
@Composable
private fun RecurrenceFieldPreview(
    @PreviewParameter(RecurrenceFieldPreviewProvider::class) scenario: RecurrenceFieldPreviewScenario,
) {
    UdsTheme {
        Surface {
            RecurrenceField(
                recurrence = when (scenario) {
                    RecurrenceFieldPreviewScenario.EmptyField -> Recurrence.None
                    RecurrenceFieldPreviewScenario.FilledField -> Recurrence.Weekly
                },
                onRecurrenceChanged = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
