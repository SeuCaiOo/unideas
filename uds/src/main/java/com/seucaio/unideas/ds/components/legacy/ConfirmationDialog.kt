package com.seucaio.unideas.ds.components.legacy

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun ConfirmationDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    formatArgs: List<Any> = emptyList(),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(messageRes, *formatArgs.toTypedArray())) },
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ConfirmationDialogPreview() {
    UdsTheme {
        ConfirmationDialog(
            titleRes = android.R.string.ok,
            messageRes = android.R.string.ok,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ConfirmationDialogBlockedPreview() {
    UdsTheme {
        ConfirmationDialog(
            titleRes = android.R.string.ok,
            messageRes = android.R.string.ok,
            onDismiss = {},
            onConfirm = null,
        )
    }
}
