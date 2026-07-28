package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueTimeField(
    dueTime: LocalTime?,
    onDueTimeChanged: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    DateFieldButton(
        valueLabel = dueTime?.format(timeFormatter),
        defaultValue = stringResource(R.string.item_form_time_label),
        onClick = { showTimePicker = true },
        onClear = { onDueTimeChanged(null) },
        clearContentDescription = stringResource(R.string.item_form_time_clear),
        modifier = modifier
    )

    if (showTimePicker) {
        val initial = dueTime ?: LocalTime.NOON
        val timePickerState = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = true,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface {
                Column(Modifier.padding(24.dp)) {
                    TimePicker(state = timePickerState)
                    TextButton(
                        onClick = {
                            onDueTimeChanged(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            showTimePicker = false
                        },
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DueTimeFieldPreview() {
    UdsTheme {
        Surface {
            DueTimeField(
                dueTime = LocalTime.of(14, 30),
                onDueTimeChanged = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
