package com.seucaio.unideas.feature.home.features.home.screen.components.chrome

import androidx.compose.runtime.Composable
import com.seucaio.unideas.core.backup.DisableSyncConfirmBottomSheet
import com.seucaio.unideas.core.backup.RestorePromptBottomSheet
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncDialogState
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncEvent
import com.seucaio.unideas.ds.components.legacy.ConfirmationBottomSheet
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeDialogState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.priority.screen.PriorityBottomSheet

@Composable
internal fun HomeDialogs(
    showPriorityBottomSheet: Boolean,
    onPriorityBottomSheetDismiss: () -> Unit,
    dialogState: HomeDialogState,
    backupSyncDialogState: BackupSyncDialogState,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onEvent: (HomeEvent) -> Unit,
    onBackupSyncEvent: (BackupSyncEvent) -> Unit,
) {
    when (backupSyncDialogState) {
        is BackupSyncDialogState.RestorePrompt -> RestorePromptBottomSheet(
            isRestoring = backupSyncDialogState.isRestoring,
            onRestoreClick = { onBackupSyncEvent(BackupSyncEvent.OnRestoreConfirmClicked) },
            onDeclineClick = { onBackupSyncEvent(BackupSyncEvent.OnRestoreDeclineClicked) },
        )
        is BackupSyncDialogState.DisableSyncConfirm -> DisableSyncConfirmBottomSheet(
            onConfirmClick = { onBackupSyncEvent(BackupSyncEvent.OnDisableSyncConfirmClicked) },
            onDeclineClick = { onBackupSyncEvent(BackupSyncEvent.OnDisableSyncDeclineClicked) },
        )
        BackupSyncDialogState.None -> Unit
    }

    if (showPriorityBottomSheet) {
        PriorityBottomSheet(
            onDismiss = onPriorityBottomSheetDismiss,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
        )
    }

    if (dialogState is HomeDialogState.DeleteSelectedConfirm) {
        ConfirmationBottomSheet(
            titleRes = R.string.home_delete_selected_title,
            messageRes = R.string.home_delete_selected_message,
            onDismiss = { onEvent(HomeEvent.OnDeleteDialogDismissed) },
            onConfirm = { onEvent(HomeEvent.OnDeleteSelectedConfirmClicked) },
        )
    }
}
