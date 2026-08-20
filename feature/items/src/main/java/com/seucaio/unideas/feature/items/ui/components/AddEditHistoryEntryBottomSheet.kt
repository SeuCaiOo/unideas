package com.seucaio.unideas.feature.items.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.seucaio.unideas.core.common.extensions.toEpochMilliUtc
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.core.common.extensions.toLocalDateUtc
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val COMPLETED_AT_TIME = LocalTime.NOON

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHistoryEntryBottomSheet(
    existing: ItemCompletionHistory?,
    blockedDates: Set<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (scheduledDate: LocalDate, completedAt: LocalDateTime?, note: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        AddEditHistoryEntrySheetContent(existing = existing, blockedDates = blockedDates, onConfirm = onConfirm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHistoryEntrySheetContent(
    existing: ItemCompletionHistory?,
    onConfirm: (scheduledDate: LocalDate, completedAt: LocalDateTime?, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    blockedDates: Set<LocalDate> = emptySet(),
) {
    var scheduledDate by remember { mutableStateOf(existing?.scheduledDate) }
    var completed by remember { mutableStateOf(existing?.completedAt != null) }
    var note by remember { mutableStateOf(existing?.note.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(
                if (existing == null) R.string.item_history_add_entry else R.string.item_history_edit_entry,
            ),
            style = MaterialTheme.typography.titleLarge,
        )

        DateFieldButton(
            valueLabel = scheduledDate?.toFormattedDateString(),
            defaultValue = stringResource(R.string.item_history_date_label),
            onClick = { showDatePicker = true },
            onClear = { scheduledDate = null },
            clearContentDescription = stringResource(R.string.item_form_date_clear),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.item_history_completed_toggle))
            Switch(checked = completed, onCheckedChange = { completed = it })
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.item_detail_note_optional_label)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val date = scheduledDate ?: return@Button
                val completedAt = if (completed) date.atTime(COMPLETED_AT_TIME) else null
                onConfirm(date, completedAt, note.ifBlank { null })
            },
            enabled = scheduledDate != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.item_history_save))
        }
    }

    if (showDatePicker) {
        HistoryEntryDatePickerDialog(
            scheduledDate = scheduledDate,
            blockedDates = blockedDates,
            onDismiss = { showDatePicker = false },
            onConfirm = { scheduledDate = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryEntryDatePickerDialog(
    scheduledDate: LocalDate?,
    blockedDates: Set<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val selectableDates = remember(blockedDates) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcTimeMillis.toLocalDateUtc()
                return !date.isAfter(LocalDate.now()) && date !in blockedDates
            }
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = scheduledDate?.toEpochMilliUtc(),
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.toLocalDateUtc()?.let(onConfirm)
                onDismiss()
            }) {
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

private class AddEditHistoryEntryPreviewProvider : PreviewParameterProvider<ItemCompletionHistory?> {
    override val values: Sequence<ItemCompletionHistory?> = sequenceOf(
        null,
        ItemCompletionHistory(
            id = 1L,
            itemId = 1L,
            scheduledDate = LocalDate.of(2026, 8, 14),
            completedAt = LocalDateTime.of(2026, 8, 14, 9, 12),
            note = "Sem tempo no dia",
        ),
    )
}

@PreviewLightDark
@Composable
private fun AddEditHistoryEntrySheetContentPreview(
    @PreviewParameter(AddEditHistoryEntryPreviewProvider::class) existing: ItemCompletionHistory?,
) {
    UdsTheme {
        Surface {
            AddEditHistoryEntrySheetContent(existing = existing, onConfirm = { _, _, _ -> })
        }
    }
}
