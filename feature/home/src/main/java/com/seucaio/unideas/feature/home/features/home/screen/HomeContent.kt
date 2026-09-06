package com.seucaio.unideas.feature.home.features.home.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.seucaio.unideas.core.backup.viewmodel.sync.BackupSyncDialogState
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.screen.components.chrome.HomeDialogs
import com.seucaio.unideas.feature.home.features.home.screen.components.chrome.HomeFab
import com.seucaio.unideas.feature.home.features.home.screen.components.chrome.HomeTopBar
import com.seucaio.unideas.feature.home.features.home.screen.components.filters.ItemsFiltersBar
import com.seucaio.unideas.feature.home.features.home.screen.components.filters.TasksNotesTabRow
import com.seucaio.unideas.feature.home.features.home.screen.components.items.ItemsContent
import com.seucaio.unideas.feature.home.features.home.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeDialogState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiState

@Composable
internal fun HomeContent(
    state: HomeScreenState,
    onEvent: (HomeEvent) -> Unit,
    navActions: HomeNavActions,
    snackbarHostState: SnackbarHostState,
    backupSync: HomeBackupSyncUi = HomeBackupSyncUi(),
) {
    val updatedOnNavigateBack by rememberUpdatedState(navActions.onNavigateBack)
    var addMenuExpanded by remember { mutableStateOf(false) }
    var showPriorityBottomSheet by rememberSaveable { mutableStateOf(false) }
    val isSnackbarVisible = snackbarHostState.currentSnackbarData != null
    val isPreview = LocalInspectionMode.current
    var syncGateResolved by remember { mutableStateOf(ColdStartSyncGate.resolved) }

    LaunchedEffect(backupSync.checkCompleted) {
        if (backupSync.checkCompleted) {
            ColdStartSyncGate.resolved = true
            syncGateResolved = true
        }
    }

    LaunchedEffect(
        state.uiState,
        backupSync.checkCompleted,
        backupSync.dialogState,
        isSnackbarVisible
    ) {
        if (isPreview) return@LaunchedEffect
        val ready = isReadyForPriorityPrompt(
            uiState = state.uiState,
            syncCheckCompleted = backupSync.checkCompleted,
            backupSyncDialogState = backupSync.dialogState,
            isSnackbarVisible = isSnackbarVisible,
        )
        if (!ColdStartPriorityPrompt.shown && ready) {
            ColdStartPriorityPrompt.shown = true
            showPriorityBottomSheet = true
        }
    }

    HomeDialogs(
        showPriorityBottomSheet = showPriorityBottomSheet,
        onPriorityBottomSheetDismiss = { showPriorityBottomSheet = false },
        dialogState = state.dialogState,
        backupSyncDialogState = backupSync.dialogState,
        onNavigateToDetail = navActions.onNavigateToDetail,
        onNavigateToAllPriorities = navActions.onNavigateToAllPriorities,
        onEvent = onEvent,
        onBackupSyncEvent = backupSync.onEvent,
    )

    Scaffold(
        topBar = {
            HomeTopBar(
                homeMode = state.homeMode,
                itemsState = state.itemsState,
                hasAnyPriorityItem = (state.uiState as? HomeUiState.Success)?.hasAnyPriorityItem == true,
                onNavigateBack = updatedOnNavigateBack,
                onShowPriorities = { showPriorityBottomSheet = true },
                onNavigateToSettings = navActions.onNavigateToSettings,
                onEvent = onEvent,
            )
        },
        floatingActionButton = {
            HomeFab(
                visible = state.uiState is HomeUiState.Success,
                homeMode = state.homeMode,
                addMenuExpanded = addMenuExpanded,
                onAddMenuExpandedChange = { addMenuExpanded = it },
                onEvent = onEvent,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeBody(
            uiState = state.uiState,
            syncGateResolved = syncGateResolved,
            filterState = state.filterState,
            itemsState = state.itemsState,
            homeMode = state.homeMode,
            isRefreshing = state.isRefreshing,
            padding = padding,
            onEvent = onEvent,
            onNavigateToArchivedItems = navActions.onNavigateToArchivedItems,
        )
    }
}

private fun isReadyForPriorityPrompt(
    uiState: HomeUiState,
    syncCheckCompleted: Boolean,
    backupSyncDialogState: BackupSyncDialogState,
    isSnackbarVisible: Boolean,
): Boolean = uiState is HomeUiState.Success &&
    uiState.hasAnyPriorityItem &&
    syncCheckCompleted &&
    backupSyncDialogState == BackupSyncDialogState.None &&
    !isSnackbarVisible

@Composable
private fun HomeBody(
    uiState: HomeUiState,
    syncGateResolved: Boolean,
    filterState: FilterState,
    itemsState: HomeItemsState,
    homeMode: HomeMode,
    isRefreshing: Boolean,
    padding: PaddingValues,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToArchivedItems: () -> Unit,
) {
    when (uiState) {
        is HomeUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
        is HomeUiState.Error ->
            UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(HomeEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
        is HomeUiState.Success ->
            if (!syncGateResolved) {
                UnideasLoadingContent(modifier = Modifier.padding(padding))
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { onEvent(HomeEvent.OnRefreshRequested) },
                    modifier = Modifier.padding(padding),
                ) {
                    HomeSuccessBody(
                        hasAnyItem = uiState.hasAnyItem,
                        hasAnyArchivedItem = uiState.hasAnyArchivedItem,
                        filterState = filterState,
                        itemsState = itemsState,
                        homeMode = homeMode,
                        onEvent = onEvent,
                        onNavigateToArchivedItems = onNavigateToArchivedItems,
                    )
                }
            }
    }
}

@Composable
private fun HomeSuccessBody(
    hasAnyItem: Boolean,
    hasAnyArchivedItem: Boolean,
    filterState: FilterState,
    itemsState: HomeItemsState,
    homeMode: HomeMode,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToArchivedItems: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TasksNotesTabRow(
            activeTab = filterState.activeTab,
            onTabSelect = { onEvent(HomeEvent.OnTabChanged(it)) },
        )
        HorizontalDivider()
        ItemsFiltersBar(
            sections = filterState.availableSections,
            tags = filterState.availableTags,
            sectionFilter = filterState.sectionFilter,
            tagFilters = filterState.tagFilters,
            onSectionFilterChange = { onEvent(HomeEvent.OnSectionFilterChanged(it)) },
            onTagFilterToggle = { onEvent(HomeEvent.OnTagFilterToggled(it)) }
        )
        ItemsContent(
            itemsState = itemsState,
            sectionFilter = filterState.sectionFilter,
            hasAnyItem = hasAnyItem,
            onEvent = onEvent,
            homeMode = homeMode,
            footer = if (hasAnyArchivedItem) {
                {
                    NavRow(
                        icon = Icons.Outlined.Archive,
                        label = stringResource(R.string.home_archived_items_action),
                        onClick = onNavigateToArchivedItems,
                    )
                }
            } else {
                null
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeContentPreview(
    @PreviewParameter(HomeContentPreviewProvider::class) scenario: HomeContentPreviewScenario,
) {
    UdsTheme {
        HomeContent(
            state = HomeScreenState(
                uiState = HomeUiState.Success(
                    hasAnyItem = scenario.fixture.hasAnyItem,
                    hasAnyPriorityItem = true,
                    hasAnyArchivedItem = scenario.hasAnyArchivedItem,
                ),
                filterState = scenario.fixture.filterState,
                itemsState = scenario.fixture.itemsState,
                homeMode = scenario.homeMode,
                dialogState = HomeDialogState.None,
                isRefreshing = false,
            ),
            onEvent = {},
            navActions = HomeNavActions(
                onNavigateBack = {},
                onNavigateToDetail = {},
                onNavigateToAllPriorities = {},
                onNavigateToSettings = {},
                onNavigateToArchivedItems = {},
            ),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
