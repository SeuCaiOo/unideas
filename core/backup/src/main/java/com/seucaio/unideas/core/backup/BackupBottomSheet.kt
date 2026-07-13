package com.seucaio.unideas.core.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.viewmodel.BackupAction
import com.seucaio.unideas.core.backup.viewmodel.BackupEvent
import com.seucaio.unideas.core.backup.viewmodel.BackupUiAction
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import com.seucaio.unideas.core.common.extensions.restartApplication
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupBottomSheet(
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val resources by rememberUpdatedState(LocalResources.current)

    var pendingAction by remember { mutableStateOf<BackupAction?>(null) }
    var restoreBackups by remember { mutableStateOf<List<BackupInfo>>(emptyList()) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val action = pendingAction ?: return@rememberLauncherForActivityResult
        val account = runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data).result
        }.getOrNull()
        viewModel.onEvent(BackupEvent.OnGoogleSignInResult(account, action))
        pendingAction = null
    }

    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
            when (action) {
                is BackupUiAction.ShowSnackbar -> {
                    // Dismiss first — the sheet would otherwise sit on top of the snackbar,
                    // hiding it from view. snackbarHostState belongs to the caller's Scaffold,
                    // so the message keeps showing after the sheet is gone.
                    onDismiss()
                    snackbarHostState.showSnackbar(resources.getString(action.message))
                }
                is BackupUiAction.LaunchGoogleSignIn -> {
                    pendingAction = action.pendingAction
                    signInLauncher.launch(action.intent)
                }
                is BackupUiAction.ShowRestoreDialog ->
                    restoreBackups = action.backups
                is BackupUiAction.RestoreCompleted -> {
                    onDismiss()
                    // A simple activity restart (finishAffinity()) is not enough here — confirmed
                    // on-device the process can survive it, leaving Koin's cached UnideasDatabase/DAO
                    // singletons pointing at the closed pre-restore database, so every query after
                    // "restart" fails forever with the same cancelled InvalidationTracker Job.
                    context.restartApplication()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        BackupSheetContent(
            uiState = uiState,
            onConnectClick = { viewModel.onEvent(BackupEvent.OnConnectClick) },
            onBackupClick = { viewModel.onEvent(BackupEvent.OnBackupClick) },
            onSyncClick = { viewModel.onEvent(BackupEvent.OnSyncClick) },
        )

        if (restoreBackups.isNotEmpty()) {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            RestoreBackupList(
                backups = restoreBackups,
                onSelect = { fileId ->
                    restoreBackups = emptyList()
                    if (account != null) {
                        viewModel.onEvent(BackupEvent.OnRestoreConfirmed(account, fileId))
                    }
                },
                onDismiss = { restoreBackups = emptyList() },
            )
        }
    }
}

@Composable
private fun BackupSheetContent(
    uiState: BackupUiState,
    onConnectClick: () -> Unit,
    onBackupClick: () -> Unit,
    onSyncClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.backup_section_title),
            style = MaterialTheme.typography.titleLarge,
        )

        when (uiState) {
            is BackupUiState.Loading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is BackupUiState.Ready -> if (uiState.isConnected) {
                ConnectedBackupContent(uiState, onBackupClick, onSyncClick)
            } else {
                DisconnectedBackupContent(onConnectClick)
            }
        }
    }
}

@Composable
private fun ConnectedBackupContent(
    uiState: BackupUiState.Ready,
    onBackupClick: () -> Unit,
    onSyncClick: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm") }
    val subtitle = uiState.lastBackupAt?.format(formatter)
        ?.let { stringResource(R.string.backup_last_at, it) }
        ?: stringResource(R.string.backup_none)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onBackupClick, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.backup_action_upload))
            }
            Button(onClick = onSyncClick, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.backup_action_sync))
            }
        }
    }
}

@Composable
private fun DisconnectedBackupContent(onConnectClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.backup_not_connected),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(onClick = onConnectClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.backup_action_connect))
        }
    }
}

@Composable
private fun RestoreBackupList(
    backups: List<BackupInfo>,
    onSelect: (fileId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.backup_restore_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        backups.forEachIndexed { index, backup ->
            TextButton(
                onClick = { onSelect(backup.fileId) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = backup.createdAt.format(formatter),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (index < backups.lastIndex) HorizontalDivider()
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text(text = stringResource(R.string.action_cancel))
        }
    }
}

@PreviewLightDark
@Composable
private fun BackupSheetContentPreview(
    @PreviewParameter(BackupPreviewProvider::class) uiState: BackupUiState,
) {
    UnideasTheme {
        Surface {
            BackupSheetContent(
                uiState = uiState,
                onConnectClick = {},
                onBackupClick = {},
                onSyncClick = {},
            )
        }
    }
}

private val previewBackups = listOf(
    BackupInfo(
        fileId = "1",
        createdAt = java.time.LocalDateTime.of(2026, 5, 7, 8, 30),
        sizeBytes = 204800,
    ),
    BackupInfo(
        fileId = "2",
        createdAt = java.time.LocalDateTime.of(2026, 5, 6, 20, 15),
        sizeBytes = 198000,
    ),
)

@PreviewLightDark
@Composable
private fun RestoreBackupListPreview() {
    UnideasTheme {
        Surface {
            RestoreBackupList(
                backups = previewBackups,
                onSelect = {},
                onDismiss = {},
            )
        }
    }
}
