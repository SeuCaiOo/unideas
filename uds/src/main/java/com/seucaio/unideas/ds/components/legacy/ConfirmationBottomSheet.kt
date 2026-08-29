package com.seucaio.unideas.ds.components.legacy

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    formatArgs: List<Any> = emptyList(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        ConfirmationSheetContent(
            titleRes = titleRes,
            messageRes = messageRes,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            formatArgs = formatArgs,
        )
    }
}

@Composable
fun ConfirmationSheetContent(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    formatArgs: List<Any> = emptyList(),
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(messageRes, *formatArgs.toTypedArray()),
            style = MaterialTheme.typography.bodyLarge,
        )

        if (onConfirm != null) {
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(android.R.string.ok))
            }
        }

        OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(android.R.string.cancel))
        }
    }
}

@PreviewLightDark
@Composable
private fun ConfirmationSheetContentPreview() {
    UdsTheme {
        Surface {
            ConfirmationSheetContent(
                titleRes = android.R.string.ok,
                messageRes = android.R.string.ok,
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ConfirmationSheetContentBlockedPreview() {
    UdsTheme {
        Surface {
            ConfirmationSheetContent(
                titleRes = android.R.string.ok,
                messageRes = android.R.string.ok,
                onDismiss = {},
                onConfirm = null,
            )
        }
    }
}
