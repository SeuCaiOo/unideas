package com.seucaio.unideas.ds.components.legacy

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Confirmation dialog for deletion. Pass `onConfirm = null` when deletion is
 * blocked (e.g. linked items exist) — the caller formats [messageRes] with
 * whatever count/detail it needs via [formatArgs] (same pattern as
 * `UiAction.ShowSnackbar`); the dialog only shows Dismiss in that case.
 *
 * Exception to `:uds`'s "no `R.*` references" portability rule (see module README) —
 * `legacy/` only exists to receive `:core:ui` components verbatim while `:core:ui` is
 * being emptied out, and gets deleted once that's done, so it doesn't need to hold to
 * the portable-module contract the rest of `:uds` does.
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

@PreviewLightDark
@Composable
private fun DeleteConfirmationDialogPreview() {
    UdsTheme {
        DeleteConfirmationDialog(
            titleRes = android.R.string.ok,
            messageRes = android.R.string.ok,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun DeleteConfirmationDialogBlockedPreview() {
    UdsTheme {
        DeleteConfirmationDialog(
            titleRes = android.R.string.ok,
            messageRes = android.R.string.ok,
            onDismiss = {},
            onConfirm = null,
        )
    }
}
