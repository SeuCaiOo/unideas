package com.seucaio.unideas.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

/** Debug tooling — sends a one-off notification on either tier's channel, no real item involved. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestNotificationBottomSheet(onSend: (urgent: Boolean) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var urgent by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        TestNotificationSheetContent(
            urgent = urgent,
            onUrgentSelect = { urgent = it },
            onConfirm = { onSend(urgent) },
        )
    }
}

@Composable
private fun TestNotificationSheetContent(
    urgent: Boolean,
    onUrgentSelect: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.test_notification_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Column(modifier = Modifier.selectableGroup()) {
            TestNotificationOption(
                titleRes = R.string.test_notification_normal,
                descriptionRes = R.string.test_notification_normal_description,
                selected = !urgent,
                onSelect = { onUrgentSelect(false) },
            )
            TestNotificationOption(
                titleRes = R.string.test_notification_urgent,
                descriptionRes = R.string.test_notification_urgent_description,
                selected = urgent,
                onSelect = { onUrgentSelect(true) },
            )
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(text = stringResource(R.string.test_notification_confirm))
        }
    }
}

@Composable
private fun TestNotificationOption(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Column {
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TestNotificationSheetContentNormalPreview() {
    UdsTheme {
        Surface {
            TestNotificationSheetContent(urgent = false, onUrgentSelect = {}, onConfirm = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun TestNotificationSheetContentUrgentPreview() {
    UdsTheme {
        Surface {
            TestNotificationSheetContent(urgent = true, onUrgentSelect = {}, onConfirm = {})
        }
    }
}
