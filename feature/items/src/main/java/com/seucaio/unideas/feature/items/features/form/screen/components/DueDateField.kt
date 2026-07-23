package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toEpochMilliUtc
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.core.common.extensions.toLocalDateUtc
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDateField(
    dueDate: LocalDate?,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    DateFieldButton(
        valueLabel = dueDate?.toFormattedDateString(),
        defaultValue = stringResource(R.string.item_form_date_label),
        onClick = { showDatePicker = true },
        onClear = { onEvent(ItemFormEvent.OnDueDateChanged(null)) },
        clearContentDescription = stringResource(R.string.item_form_date_clear),
        modifier = modifier
    )

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = dueDate?.toEpochMilliUtc())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newDate = datePickerState.selectedDateMillis?.toLocalDateUtc()
                    newDate?.let { onEvent(ItemFormEvent.OnDueDateChanged(it)) }
                    showDatePicker = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private val previewDueDate = LocalDate.of(2026, 7, 20)

@PreviewLightDark
@Composable
private fun DueDateFieldPreview() {
    UdsTheme {
        Surface {
            DueDateField(
                dueDate = previewDueDate,
                onEvent = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
