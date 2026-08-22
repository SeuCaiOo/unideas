package com.seucaio.unideas.core.backup

import android.content.Intent
import android.content.res.Resources
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.viewmodel.BackupAction
import com.seucaio.unideas.core.backup.viewmodel.BackupEvent
import com.seucaio.unideas.core.backup.viewmodel.BackupListStatus
import com.seucaio.unideas.core.backup.viewmodel.BackupUiAction
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import com.seucaio.unideas.core.common.extensions.restartApplication
import com.seucaio.unideas.core.common.extensions.toFormattedDateTimeString
import com.seucaio.unideas.core.common.extensions.toFormattedTimeString
import com.seucaio.unideas.ds.theme.UdsTheme
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

/**
 * `visible` gates only the sheet's own UI (and the delete-confirm dialog) — [BackupActionEffects]
 * below is always composed regardless, so its action-collecting coroutine survives a dismiss.
 * Confirmed the hard way: when the effect lived inside the same conditional as the sheet's UI,
 * calling `onDismiss()` from a `ShowSnackbar`/`RestoreCompleted` action removed this whole
 * composable from the tree before the pending `showSnackbar(...)` suspend call could complete,
 * silently swallowing the feedback (success and failure alike).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupBottomSheet(
    visible: Boolean,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources by rememberUpdatedState(LocalResources.current)

    var pendingAction by remember { mutableStateOf<BackupAction?>(null) }
    var pendingDeleteFileId by remember { mutableStateOf<String?>(null) }

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

    BackupActionEffects(
        actions = viewModel.action,
        snackbarHostState = snackbarHostState,
        resources = resources,
        onDismiss = onDismiss,
        onLaunchSignIn = { intent, action ->
            pendingAction = action
            signInLauncher.launch(intent)
        },
        onShowDeleteConfirm = { pendingDeleteFileId = it },
    )

    if (visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            BackupSheetContent(
                uiState = uiState,
                onConnectClick = { viewModel.onEvent(BackupEvent.OnConnectClick) },
                onBackupClick = { viewModel.onEvent(BackupEvent.OnBackupClick) },
                onToggleBackupListClick = { viewModel.onEvent(BackupEvent.OnToggleBackupListClick) },
                onBackupSelect = { fileId -> viewModel.onEvent(BackupEvent.OnBackupSelected(fileId)) },
                onRestoreClick = { viewModel.onEvent(BackupEvent.OnRestoreClick) },
                onDeleteBackupClick = { fileId -> viewModel.onEvent(BackupEvent.OnDeleteBackupClick(fileId)) },
                onRetryBackupListClick = { viewModel.onEvent(BackupEvent.OnRetryBackupListClick) },
            )
        }
    }

    val deleteFileId = pendingDeleteFileId
    if (deleteFileId != null) {
        DeleteBackupConfirmDialog(
            onConfirm = {
                viewModel.onEvent(BackupEvent.OnDeleteConfirmed(deleteFileId))
                pendingDeleteFileId = null
            },
            onDismiss = { pendingDeleteFileId = null },
        )
    }
}

@Composable
private fun BackupActionEffects(
    actions: Flow<BackupUiAction>,
    snackbarHostState: SnackbarHostState,
    resources: Resources,
    onDismiss: () -> Unit,
    onLaunchSignIn: (intent: Intent, pendingAction: BackupAction) -> Unit,
    onShowDeleteConfirm: (fileId: String) -> Unit,
) {
    val context = LocalContext.current
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnLaunchSignIn by rememberUpdatedState(onLaunchSignIn)
    val currentOnShowDeleteConfirm by rememberUpdatedState(onShowDeleteConfirm)

    LaunchedEffect(actions) {
        actions.collect { action ->
            when (action) {
                is BackupUiAction.ShowSnackbar -> {
                    // Dismiss first — the sheet would otherwise sit on top of the snackbar,
                    // hiding it from view. snackbarHostState belongs to the caller's Scaffold,
                    // so the message keeps showing after the sheet is gone.
                    currentOnDismiss()
                    snackbarHostState.showSnackbar(resources.getString(action.message))
                }
                is BackupUiAction.LaunchGoogleSignIn ->
                    currentOnLaunchSignIn(action.intent, action.pendingAction)
                is BackupUiAction.ShowDeleteConfirm -> currentOnShowDeleteConfirm(action.fileId)
                is BackupUiAction.RestoreCompleted -> {
                    currentOnDismiss()
                    // A simple activity restart (finishAffinity()) is not enough here — confirmed
                    // on-device the process can survive it, leaving Koin's cached UnideasDatabase/DAO
                    // singletons pointing at the closed pre-restore database, so every query after
                    // "restart" fails forever with the same cancelled InvalidationTracker Job.
                    context.restartApplication()
                }
            }
        }
    }
}

@Composable
private fun DeleteBackupConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.backup_delete_confirm_title)) },
        text = { Text(text = stringResource(R.string.backup_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = stringResource(R.string.backup_delete_confirm_action)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun BackupSheetContent(
    uiState: BackupUiState,
    onConnectClick: () -> Unit,
    onBackupClick: () -> Unit,
    onToggleBackupListClick: () -> Unit,
    onBackupSelect: (fileId: String) -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteBackupClick: (fileId: String) -> Unit,
    onRetryBackupListClick: () -> Unit,
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
                ConnectedBackupContent(
                    uiState = uiState,
                    onBackupClick = onBackupClick,
                    onToggleBackupListClick = onToggleBackupListClick,
                    onBackupSelect = onBackupSelect,
                    onRestoreClick = onRestoreClick,
                    onDeleteBackupClick = onDeleteBackupClick,
                    onRetryBackupListClick = onRetryBackupListClick,
                )
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
    onToggleBackupListClick: () -> Unit,
    onBackupSelect: (fileId: String) -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteBackupClick: (fileId: String) -> Unit,
    onRetryBackupListClick: () -> Unit,
) {
    val subtitle = uiState.lastBackupAt?.toFormattedDateTimeString()
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
            OutlinedButton(onClick = onToggleBackupListClick, modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (uiState.isBackupListVisible) {
                            R.string.backup_action_hide_backups
                        } else {
                            R.string.backup_action_view_backups
                        },
                    ),
                )
            }
        }

        if (uiState.isBackupListVisible) {
            BackupListSection(
                status = uiState.backupListStatus,
                selectedFileId = uiState.selectedBackupFileId,
                onBackupSelect = onBackupSelect,
                onDeleteBackupClick = onDeleteBackupClick,
                onRestoreClick = onRestoreClick,
                onRetryClick = onRetryBackupListClick,
            )
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
private fun BackupListSection(
    status: BackupListStatus,
    selectedFileId: String?,
    onBackupSelect: (fileId: String) -> Unit,
    onDeleteBackupClick: (fileId: String) -> Unit,
    onRestoreClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.backup_backups_saved_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (status) {
            BackupListStatus.Empty ->
                BackupListMessage(text = stringResource(R.string.backup_no_backups_found))
            BackupListStatus.Error ->
                BackupListMessage(
                    text = stringResource(R.string.backup_list_load_error),
                    onRetryClick = onRetryClick,
                )
            is BackupListStatus.Loaded -> {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = BACKUP_LIST_MAX_HEIGHT)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(status.backups, key = { _, backup -> backup.fileId }) { index, backup ->
                        BackupListItemRow(
                            backup = backup,
                            selected = backup.fileId == selectedFileId,
                            isMostRecent = index == 0,
                            onSelect = { onBackupSelect(backup.fileId) },
                            onDelete = { onDeleteBackupClick(backup.fileId) },
                        )
                    }
                }

                Button(
                    onClick = onRestoreClick,
                    enabled = selectedFileId != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.backup_action_restore))
                }
            }
        }
    }
}

@Composable
private fun BackupListMessage(
    text: String,
    modifier: Modifier = Modifier,
    onRetryClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BACKUP_LIST_MESSAGE_MIN_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (onRetryClick != null) {
            TextButton(onClick = onRetryClick, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = stringResource(R.string.backup_list_retry))
            }
        }
    }
}

@Composable
private fun BackupListItemRow(
    backup: BackupInfo,
    selected: Boolean,
    isMostRecent: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RadioButton(selected = selected, onClick = null)

            Column(modifier = Modifier.weight(1f)) {
                val date = backup.createdAt.toLocalDate()
                val today = LocalDate.now()
                val dateLabel = when (date) {
                    today -> stringResource(R.string.backup_date_today, backup.createdAt.toFormattedTimeString())
                    today.minusDays(1) ->
                        stringResource(R.string.backup_date_yesterday, backup.createdAt.toFormattedTimeString())
                    else -> backup.createdAt.toFormattedDateTimeString()
                }
                Text(text = dateLabel, style = MaterialTheme.typography.bodyMedium)
                if (isMostRecent) {
                    Text(
                        text = stringResource(R.string.backup_most_recent),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!isMostRecent) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.backup_delete_confirm_title),
                    )
                }
            }
        }
    }
}

private val BACKUP_LIST_MAX_HEIGHT = 260.dp
private val BACKUP_LIST_MESSAGE_MIN_HEIGHT = 96.dp

@PreviewLightDark
@Composable
private fun BackupSheetContentPreview(
    @PreviewParameter(BackupPreviewProvider::class) uiState: BackupUiState,
) {
    UdsTheme {
        Surface {
            BackupSheetContent(
                uiState = uiState,
                onConnectClick = {},
                onBackupClick = {},
                onToggleBackupListClick = {},
                onBackupSelect = {},
                onRestoreClick = {},
                onDeleteBackupClick = {},
                onRetryBackupListClick = {},
            )
        }
    }
}
