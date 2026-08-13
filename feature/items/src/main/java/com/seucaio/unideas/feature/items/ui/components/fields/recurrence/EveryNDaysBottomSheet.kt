package com.seucaio.unideas.feature.items.ui.components.fields.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

private const val MIN_EVERY_N_DAYS = 2
private const val MAX_EVERY_N_DAYS = 99

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EveryNDaysBottomSheet(
    days: Int,
    onDaysConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        EveryNDaysSheetContent(days = days, onDaysConfirmed = onDaysConfirmed)
    }
}

@Composable
private fun EveryNDaysSheetContent(
    days: Int,
    onDaysConfirmed: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(days) { mutableIntStateOf(days) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.item_form_recurrence_every_n_days_title),
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { value-- }, enabled = value > MIN_EVERY_N_DAYS) {
                Icon(
                    Icons.Outlined.Remove,
                    contentDescription = stringResource(R.string.item_form_recurrence_every_n_days_decrease)
                )
            }
            Text(
                text = stringResource(R.string.item_form_recurrence_every_n_days, value),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            IconButton(onClick = { value++ }, enabled = value < MAX_EVERY_N_DAYS) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.item_form_recurrence_every_n_days_increase)
                )
            }
        }

        Button(onClick = { onDaysConfirmed(value) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.item_form_recurrence_confirm))
        }
    }
}

@PreviewLightDark
@Composable
private fun EveryNDaysBottomSheetPreview() {
    UdsTheme {
        Surface {
            EveryNDaysSheetContent(days = Recurrence.EveryNDays.EVERY_OTHER_DAY_DAYS, onDaysConfirmed = {})
        }
    }
}
