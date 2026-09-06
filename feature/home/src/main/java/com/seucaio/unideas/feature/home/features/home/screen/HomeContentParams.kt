package com.seucaio.unideas.feature.home.features.home.screen

import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncDialogState
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeDialogState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiState

internal data class HomeScreenState(
    val uiState: HomeUiState,
    val filterState: FilterState,
    val itemsState: HomeItemsState,
    val homeMode: HomeMode,
    val dialogState: HomeDialogState,
    val isRefreshing: Boolean,
)

internal data class HomeNavActions(
    val onNavigateBack: (() -> Unit)?,
    val onNavigateToDetail: (Long) -> Unit,
    val onNavigateToAllPriorities: () -> Unit,
    val onNavigateToSettings: () -> Unit,
    val onNavigateToArchivedItems: () -> Unit,
)

internal data class HomeBackupSyncUi(
    val dialogState: BackupSyncDialogState = BackupSyncDialogState.None,
    val checkCompleted: Boolean = true,
    val onEvent: (BackupSyncEvent) -> Unit = {},
)
