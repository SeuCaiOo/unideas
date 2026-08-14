package com.seucaio.unideas.feature.items.ui.screens.detail

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.core.common.extensions.toEpochMilliUtc
import com.seucaio.unideas.core.common.extensions.toLocalDateUtc
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendDeadlineDatePickerDialog(
    currentDueDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDueDate.plusDays(1).toEpochMilliUtc(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val newDueDate = datePickerState.selectedDateMillis?.toLocalDateUtc()
                    newDueDate?.let(onConfirm)
                },
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
