package com.seucaio.unideas.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupBottomSheet(
    backupCreatedAt: String,
    onRestoreClick: () -> Unit,
    onStartFreshClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onStartFreshClick,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        RestoreBackupSheetContent(
            backupCreatedAt = backupCreatedAt,
            onRestoreClick = onRestoreClick,
            onStartFreshClick = onStartFreshClick,
        )
    }
}

@Composable
fun RestoreBackupSheetContent(
    backupCreatedAt: String,
    onRestoreClick: () -> Unit,
    onStartFreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.restore_sheet_question),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.restore_sheet_backup_available),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        BackupInfoCard(backupCreatedAt)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRestoreClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.restore_sheet_restore))
            }
            TextButton(onClick = onStartFreshClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.restore_sheet_start_fresh))
            }
        }
    }
}

@Composable
private fun BackupInfoCard(backupCreatedAt: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(
                text = stringResource(R.string.restore_sheet_backup_date, backupCreatedAt),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.restore_sheet_backup_location),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RestoreBackupSheetContentPreview() {
    UdsTheme {
        Surface {
            RestoreBackupSheetContent(
                backupCreatedAt = "12/08/2026 09:30",
                onRestoreClick = {},
                onStartFreshClick = {},
            )
        }
    }
}
