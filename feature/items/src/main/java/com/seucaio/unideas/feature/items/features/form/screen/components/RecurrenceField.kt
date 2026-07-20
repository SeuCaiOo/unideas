package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.DropdownField
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent

@Composable
fun RecurrenceFieldV2(recurrence: Recurrence, onEvent: (ItemFormEvent) -> Unit, modifier: Modifier = Modifier) {
    val dailyLabel = stringResource(R.string.item_form_recurrence_daily)
    val weeklyLabel = stringResource(R.string.item_form_recurrence_weekly)
    val monthlyLabel = stringResource(R.string.item_form_recurrence_monthly)
    val noneLabel = stringResource(R.string.item_form_recurrence_none)

    FormField(label = stringResource(R.string.item_form_recurrence_label), modifier = modifier) {
        DropdownField(
            options = listOf(dailyLabel, weeklyLabel, monthlyLabel),
            selected = when (recurrence) {
                Recurrence.Daily -> dailyLabel
                Recurrence.Weekly -> weeklyLabel
                Recurrence.Monthly -> monthlyLabel
                else -> ""
            },
            emptyOptionLabel = noneLabel,
            onSelect = { label ->
                val newRecurrence = when (label) {
                    dailyLabel -> Recurrence.Daily
                    weeklyLabel -> Recurrence.Weekly
                    monthlyLabel -> Recurrence.Monthly
                    else -> Recurrence.None
                }
                onEvent(ItemFormEvent.OnRecurrenceChanged(newRecurrence))
            },
        )
    }
}
