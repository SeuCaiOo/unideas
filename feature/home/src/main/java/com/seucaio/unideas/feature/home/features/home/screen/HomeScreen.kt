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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.ItemType
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
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Plain in-memory flag, not saved-instance-state — must reset on every fresh process,
 * including when the OS restores a killed process from SavedStateHandle, so it can't
 * live in `rememberSaveable`/a Bundle.
 */
private object ColdStartPriorityPrompt {
    var shown = false
}

@Composable
fun HomeScreen(
    savedStateHandle: SavedStateHandle,
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToDetailForLateCompletion: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchivedItems: () -> Unit,
    onNavigateToBackupSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backupUiState by backupViewModel.uiState.collectAsStateWithLifecycle()
    val isAutoBackupEnabled = (backupUiState as? BackupUiState.Ready)?.isAutoBackupEnabled == true
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val homeMode by viewModel.homeMode.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToDetailForLateCompletion by rememberUpdatedState(onNavigateToDetailForLateCompletion)
    val updatedOnNavigateToAddItem by rememberUpdatedState(onNavigateToAddItem)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)
    val updatedOnNavigateToSettings by rememberUpdatedState(onNavigateToSettings)
    val updatedOnNavigateToArchivedItems by rememberUpdatedState(onNavigateToArchivedItems)
    val updatedOnNavigateToBackupSettings by rememberUpdatedState(onNavigateToBackupSettings)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HomeUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is HomeUiAction.NavigateToDetailForLateCompletion ->
                    updatedOnNavigateToDetailForLateCompletion(action.itemId)
                is HomeUiAction.NavigateToAddItem -> updatedOnNavigateToAddItem(action.type)
                is HomeUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    val itemSaved by remember(savedStateHandle) {
        savedStateHandle.getStateFlow(Constants.ITEM_SAVED_RESULT_KEY, false)
    }.collectAsStateWithLifecycle()
    val updatedItemSaved by rememberUpdatedState(itemSaved)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasChanges = updatedItemSaved
                if (hasChanges) savedStateHandle[Constants.ITEM_SAVED_RESULT_KEY] = false
                viewModel.onEvent(HomeEvent.OnScreenResumed(hasChanges))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeContent(
        uiState = uiState,
        filterState = filterState,
        itemsState = itemsState,
        homeMode = homeMode,
        dialogState = dialogState,
        isRefreshing = isRefreshing,
        isAutoBackupEnabled = isAutoBackupEnabled,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = updatedOnNavigateToDetail,
        onNavigateToAllPriorities = updatedOnNavigateToAllPriorities,
        onNavigateToSettings = updatedOnNavigateToSettings,
        onNavigateToArchivedItems = updatedOnNavigateToArchivedItems,
        onShowBackup = updatedOnNavigateToBackupSettings,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    filterState: FilterState,
    itemsState: HomeItemsState,
    homeMode: HomeMode,
    dialogState: HomeDialogState,
    isRefreshing: Boolean,
    isAutoBackupEnabled: Boolean,
    onEvent: (HomeEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchivedItems: () -> Unit,
    onShowBackup: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    var addMenuExpanded by remember { mutableStateOf(false) }
    var showPriorityBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        val state = uiState
        if (!ColdStartPriorityPrompt.shown && state is HomeUiState.Success && state.hasAnyPriorityItem) {
            ColdStartPriorityPrompt.shown = true
            showPriorityBottomSheet = true
        }
    }

    HomeDialogs(
        showPriorityBottomSheet = showPriorityBottomSheet,
        onPriorityBottomSheetDismiss = { showPriorityBottomSheet = false },
        dialogState = dialogState,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToAllPriorities = onNavigateToAllPriorities,
        onEvent = onEvent,
    )

    Scaffold(
        topBar = {
            HomeTopBar(
                homeMode = homeMode,
                itemsState = itemsState,
                hasAnyPriorityItem = (uiState as? HomeUiState.Success)?.hasAnyPriorityItem == true,
                isAutoBackupEnabled = isAutoBackupEnabled,
                onNavigateBack = updatedOnNavigateBack,
                onShowPriorities = { showPriorityBottomSheet = true },
                onNavigateToSettings = onNavigateToSettings,
                onShowBackup = onShowBackup,
                onEvent = onEvent,
            )
        },
        floatingActionButton = {
            HomeFab(
                visible = uiState is HomeUiState.Success,
                homeMode = homeMode,
                addMenuExpanded = addMenuExpanded,
                onAddMenuExpandedChange = { addMenuExpanded = it },
                onEvent = onEvent,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeBody(
            uiState = uiState,
            filterState = filterState,
            itemsState = itemsState,
            homeMode = homeMode,
            isRefreshing = isRefreshing,
            padding = padding,
            onEvent = onEvent,
            onNavigateToArchivedItems = onNavigateToArchivedItems,
        )
    }
}

@Composable
private fun HomeBody(
    uiState: HomeUiState,
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
private fun HomeScreenPreview(
    @PreviewParameter(HomePreviewProvider::class) fixture: HomePreviewFixture,
) {
    UdsTheme {
        HomeContent(
            uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem, hasAnyPriorityItem = true),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            homeMode = HomeMode.Normal,
            dialogState = HomeDialogState.None,
            isRefreshing = false,
            isAutoBackupEnabled = true,
            onEvent = {},
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAllPriorities = {},
            onNavigateToSettings = {},
            onNavigateToArchivedItems = {},
            onShowBackup = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenArchivedFooterPreview(
    @PreviewParameter(HomePreviewProvider::class) fixture: HomePreviewFixture,
) {
    UdsTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                hasAnyItem = fixture.hasAnyItem,
                hasAnyPriorityItem = true,
                hasAnyArchivedItem = true,
            ),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            homeMode = HomeMode.Normal,
            dialogState = HomeDialogState.None,
            isRefreshing = false,
            isAutoBackupEnabled = true,
            onEvent = {},
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAllPriorities = {},
            onNavigateToSettings = {},
            onNavigateToArchivedItems = {},
            onShowBackup = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenSelectionModePreview(
    @PreviewParameter(HomePreviewProvider::class) fixture: HomePreviewFixture,
) {
    UdsTheme {
        HomeContent(
            uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem, hasAnyPriorityItem = true),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            homeMode = HomeMode.Selection(fixture.itemsState.tabItems.take(2).map { it.id }.toSet()),
            dialogState = HomeDialogState.None,
            isRefreshing = false,
            isAutoBackupEnabled = true,
            onEvent = {},
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAllPriorities = {},
            onNavigateToSettings = {},
            onNavigateToArchivedItems = {},
            onShowBackup = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
