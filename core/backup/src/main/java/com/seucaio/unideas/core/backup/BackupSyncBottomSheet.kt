package com.seucaio.unideas.core.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberFixedSheetState(): SheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { it != SheetValue.Hidden },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestorePromptBottomSheet(onRestoreClick: () -> Unit, onDeclineClick: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = rememberFixedSheetState(),
        properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false),
    ) {
        RestorePromptSheetContent(onRestoreClick = onRestoreClick, onDeclineClick = onDeclineClick)
    }
}

@Composable
fun RestorePromptSheetContent(
    onRestoreClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.backup_sync_restore_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.backup_sync_restore_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRestoreClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.backup_sync_restore_action))
            }
            TextButton(onClick = onDeclineClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.backup_sync_restore_decline))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisableSyncConfirmBottomSheet(onConfirmClick: () -> Unit, onDeclineClick: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = rememberFixedSheetState(),
        properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false),
    ) {
        DisableSyncConfirmSheetContent(
            onConfirmClick = onConfirmClick,
            onDeclineClick = onDeclineClick
        )
    }
}

@Composable
fun DisableSyncConfirmSheetContent(
    onConfirmClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.backup_sync_disable_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.backup_sync_disable_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onConfirmClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.backup_sync_disable_confirm))
            }
            TextButton(onClick = onDeclineClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.backup_sync_disable_decline))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun RestorePromptSheetContentPreview() {
    UdsTheme {
        Surface {
            RestorePromptSheetContent(onRestoreClick = {}, onDeclineClick = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun DisableSyncConfirmSheetContentPreview() {
    UdsTheme {
        Surface {
            DisableSyncConfirmSheetContent(onConfirmClick = {}, onDeclineClick = {})
        }
    }
}
