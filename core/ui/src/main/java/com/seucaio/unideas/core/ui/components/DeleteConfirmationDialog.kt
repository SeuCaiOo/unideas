package com.seucaio.unideas.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/**
 * Confirmation dialog for deletion. Pass `onConfirm = null` when deletion is
 * blocked (e.g. linked items exist) — the caller formats [messageRes] with
 * whatever count/detail it needs via [formatArgs] (same pattern as
 * `UiAction.ShowSnackbar`); the dialog only shows Dismiss in that case.
 */
@Composable
fun DeleteConfirmationDialog(
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

@Preview
@Composable
private fun DeleteConfirmationDialogPreview() {
    UnideasTheme {
        DeleteConfirmationDialog(
            titleRes = android.R.string.ok,
            messageRes = android.R.string.ok,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun DeleteConfirmationDialogBlockedPreview() {
    UnideasTheme {
        DeleteConfirmationDialog(
            titleRes = android.R.string.ok,
            messageRes = android.R.string.ok,
            onDismiss = {},
            onConfirm = null,
        )
    }
}
