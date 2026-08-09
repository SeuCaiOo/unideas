package com.seucaio.unideas.feature.items.ui.components.fields

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.ds.components.inputs.DropdownField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

/**
 * Fixed presets rather than free-form days — keeps the picker a simple dropdown. Requests
 * `POST_NOTIFICATIONS` (Android 13+) the first time this field is shown, since that's the first
 * point the user is actually configuring a reminder (#115) — no internal precedent to follow,
 * this is the project's first runtime permission request.
 */
@Composable
fun ReminderWarningField(
    reminderWarning: ReminderWarning,
    onReminderWarningChanged: (ReminderWarning) -> Unit,
    modifier: Modifier = Modifier
) {
    RequestNotificationPermission()

    val presets = listOf(
        1 to stringResource(R.string.item_form_reminder_1_day),
        2 to stringResource(R.string.item_form_reminder_2_days),
        3 to stringResource(R.string.item_form_reminder_3_days),
        7 to stringResource(R.string.item_form_reminder_7_days),
    )
    val selectedDays = (reminderWarning as? ReminderWarning.DaysBefore)?.days

    DropdownField(
        options = presets.map { it.second },
        selected = presets.firstOrNull { it.first == selectedDays }?.second.orEmpty(),
        emptyOptionLabel = stringResource(R.string.item_form_reminder_none),
        onSelect = { label ->
            val days = presets.firstOrNull { it.second == label }?.first
            onReminderWarningChanged(
                days?.let { ReminderWarning.DaysBefore(it) }
                    ?: ReminderWarning.None
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@PreviewLightDark
@Composable
private fun ReminderWarningFieldPreview() {
    UdsTheme {
        Surface {
            ReminderWarningField(
                reminderWarning = ReminderWarning.DaysBefore(2),
                onReminderWarningChanged = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
