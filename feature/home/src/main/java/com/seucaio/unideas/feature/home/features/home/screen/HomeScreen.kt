package com.seucaio.unideas.feature.home.features.home.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncEvent
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncUiAction
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncViewModel
import com.seucaio.unideas.core.common.extensions.restartApplication
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Plain in-memory flag, not saved-instance-state — must reset on every fresh process,
 * including when the OS restores a killed process from SavedStateHandle, so it can't
 * live in `rememberSaveable`/a Bundle.
 */
internal object ColdStartPriorityPrompt {
    var shown = false
}

/**
 * Plain in-memory flag, same reasoning as [ColdStartPriorityPrompt] — resolved once per process,
 * so the list only waits for the backup sync check the first time, not on every screen resume.
 */
internal object ColdStartSyncGate {
    var resolved = false
}

@Composable
fun HomeScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToDetailForLateCompletion: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchivedItems: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    backupSyncViewModel: BackupSyncViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val homeMode by viewModel.homeMode.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val backupSyncDialogState by backupSyncViewModel.dialogState.collectAsStateWithLifecycle()
    val syncCheckCompleted by backupSyncViewModel.syncCheckCompleted.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)
    val updatedOnNavigateToSettings by rememberUpdatedState(onNavigateToSettings)
    val updatedOnNavigateToArchivedItems by rememberUpdatedState(onNavigateToArchivedItems)

    HandleHomeUiActions(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToDetailForLateCompletion = onNavigateToDetailForLateCompletion,
        onNavigateToAddItem = onNavigateToAddItem,
    )

    HandleBackupSyncUiActions(backupSyncViewModel, snackbarHostState)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(HomeEvent.OnScreenResumed)
                backupSyncViewModel.onEvent(BackupSyncEvent.OnSyncCheckRequested)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeContent(
        state = HomeScreenState(
            uiState = uiState,
            filterState = filterState,
            itemsState = itemsState,
            homeMode = homeMode,
            dialogState = dialogState,
            isRefreshing = isRefreshing,
        ),
        onEvent = viewModel::onEvent,
        navActions = HomeNavActions(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = updatedOnNavigateToDetail,
            onNavigateToAllPriorities = updatedOnNavigateToAllPriorities,
            onNavigateToSettings = updatedOnNavigateToSettings,
            onNavigateToArchivedItems = updatedOnNavigateToArchivedItems,
        ),
        backupSync = HomeBackupSyncUi(
            dialogState = backupSyncDialogState,
            checkCompleted = syncCheckCompleted,
            onEvent = backupSyncViewModel::onEvent,
        ),
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun HandleHomeUiActions(
    viewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToDetailForLateCompletion: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
) {
    val currentOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val currentOnNavigateToDetailForLateCompletion by rememberUpdatedState(onNavigateToDetailForLateCompletion)
    val currentOnNavigateToAddItem by rememberUpdatedState(onNavigateToAddItem)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HomeUiAction.NavigateToDetail -> currentOnNavigateToDetail(action.itemId)
                is HomeUiAction.NavigateToDetailForLateCompletion ->
                    currentOnNavigateToDetailForLateCompletion(action.itemId)
                is HomeUiAction.NavigateToAddItem -> currentOnNavigateToAddItem(action.type)
                is HomeUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }
}

@Composable
private fun HandleBackupSyncUiActions(
    backupSyncViewModel: BackupSyncViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    LaunchedEffect(Unit) {
        backupSyncViewModel.uiAction.collect { action ->
            when (action) {
                BackupSyncUiAction.RestoreCompleted -> context.restartApplication()
                is BackupSyncUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
                is BackupSyncUiAction.ShowSnackbar ->
                    snackbarHostState.showSnackbar(resources.getString(action.messageRes))
            }
        }
    }
}
