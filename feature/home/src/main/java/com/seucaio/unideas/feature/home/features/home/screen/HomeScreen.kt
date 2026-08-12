package com.seucaio.unideas.feature.home.features.home.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.buttons.AppFab
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.screen.components.AddItemFab
import com.seucaio.unideas.feature.home.features.home.screen.components.ItemsContent
import com.seucaio.unideas.feature.home.features.home.screen.components.ItemsFiltersBar
import com.seucaio.unideas.feature.home.features.home.screen.components.TasksNotesTabRow
import com.seucaio.unideas.feature.home.features.home.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeDialogState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeViewModel
import com.seucaio.unideas.feature.home.features.priority.screen.PriorityBottomSheet
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
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val homeMode by viewModel.homeMode.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToAddItem by rememberUpdatedState(onNavigateToAddItem)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)
    val updatedOnNavigateToSettings by rememberUpdatedState(onNavigateToSettings)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HomeUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is HomeUiAction.NavigateToAddItem -> updatedOnNavigateToAddItem(action.type)
                is HomeUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    HomeContent(
        uiState = uiState,
        filterState = filterState,
        itemsState = itemsState,
        homeMode = homeMode,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = updatedOnNavigateToDetail,
        onNavigateToAllPriorities = updatedOnNavigateToAllPriorities,
        onNavigateToSettings = updatedOnNavigateToSettings,
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
    onEvent: (HomeEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    var addMenuExpanded by remember { mutableStateOf(false) }
    var showPriorityBottomSheet by rememberSaveable { mutableStateOf(false) }
    val allSelected = homeMode is HomeMode.Selection &&
        itemsState.tabItems.isNotEmpty() &&
        itemsState.tabItems.map { it.id }.toSet() == homeMode.selectedItemIds

    LaunchedEffect(Unit) {
        if (!ColdStartPriorityPrompt.shown) {
            ColdStartPriorityPrompt.shown = true
            showPriorityBottomSheet = true
        }
    }

    if (showPriorityBottomSheet) {
        PriorityBottomSheet(
            onDismiss = { showPriorityBottomSheet = false },
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
        )
    }

    if (dialogState is HomeDialogState.DeleteSelectedConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.home_delete_selected_title,
            messageRes = R.string.home_delete_selected_message,
            onDismiss = { onEvent(HomeEvent.OnDeleteDialogDismissed) },
            onConfirm = { onEvent(HomeEvent.OnDeleteSelectedConfirmClicked) },
        )
    }

    Scaffold(
        topBar = {
            if (homeMode is HomeMode.Selection) {
                UnideasTopBar(
                    title = pluralStringResource(
                        R.plurals.home_selection_count,
                        homeMode.selectedItemIds.size,
                        homeMode.selectedItemIds.size,
                    ),
                    onNavigateBack = { onEvent(HomeEvent.OnSelectionCleared) },
                    navigationBackIcon = Icons.Default.Close,
                    actions = {
                        IconButton(onClick = { onEvent(HomeEvent.OnSelectAllClicked) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                contentDescription = stringResource(
                                    if (allSelected) {
                                        R.string.home_selection_clear
                                    } else {
                                        R.string.home_selection_select_all
                                    },
                                ),
                            )
                        }
                    },
                )
            } else {
                UnideasTopBar(
                    title = stringResource(R.string.home_title),
                    onNavigateBack = updatedOnNavigateBack,
                    actions = {
                        HomeTopBarActions(
                            onShowPriorities = { showPriorityBottomSheet = true },
                            onNavigateToSettings = onNavigateToSettings,
                        )
                    },
                )
            }
        },
        floatingActionButton = {
            ConditionalFab(visible = uiState is HomeUiState.Success) {
                if (homeMode is HomeMode.Selection) {
                    AppFab(
                        icon = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.home_selection_delete),
                        onClick = { onEvent(HomeEvent.OnDeleteSelectedClicked) },
                    )
                } else {
                    AddItemFab(
                        expanded = addMenuExpanded,
                        onToggle = { addMenuExpanded = !addMenuExpanded },
                        onAddTask = {
                            addMenuExpanded = false
                            onEvent(HomeEvent.OnAddClicked(ItemType.TASK))
                        },
                        onAddNote = {
                            addMenuExpanded = false
                            onEvent(HomeEvent.OnAddClicked(ItemType.NOTE))
                        },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeBody(
            uiState = uiState,
            filterState = filterState,
            itemsState = itemsState,
            homeMode = homeMode,
            padding = padding,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun RowScope.HomeTopBarActions(onShowPriorities: () -> Unit, onNavigateToSettings: () -> Unit) {
    IconButton(onClick = onShowPriorities) {
        Icon(
            Icons.Outlined.Flag,
            contentDescription = stringResource(R.string.priority_panel_title),
        )
    }
    IconButton(onClick = onNavigateToSettings) {
        Icon(
            Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.home_settings_action),
        )
    }
}

@Composable
private fun HomeBody(
    uiState: HomeUiState,
    filterState: FilterState,
    itemsState: HomeItemsState,
    homeMode: HomeMode,
    padding: PaddingValues,
    onEvent: (HomeEvent) -> Unit,
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
            HomeSuccessBody(
                hasAnyItem = uiState.hasAnyItem,
                filterState = filterState,
                itemsState = itemsState,
                homeMode = homeMode,
                modifier = Modifier.padding(padding),
                onEvent = onEvent,
            )
    }
}

@Composable
private fun HomeSuccessBody(
    hasAnyItem: Boolean,
    filterState: FilterState,
    itemsState: HomeItemsState,
    homeMode: HomeMode,
    onEvent: (HomeEvent) -> Unit,
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
            onTagFilterToggle = { onEvent(HomeEvent.OnTagFilterToggled(it)) },
            viewMode = filterState.viewMode,
            onViewModeChange = { onEvent(HomeEvent.OnViewModeChanged(it)) },
        )
        ItemsContent(
            itemsState = itemsState,
            filterState = filterState,
            hasAnyItem = hasAnyItem,
            onEvent = onEvent,
            homeMode = homeMode,
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
            uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            homeMode = HomeMode.Normal,
            dialogState = HomeDialogState.None,
            onEvent = {},
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAllPriorities = {},
            onNavigateToSettings = {},
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
            uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            homeMode = HomeMode.Selection(fixture.itemsState.tabItems.take(2).map { it.id }.toSet()),
            dialogState = HomeDialogState.None,
            onEvent = {},
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAllPriorities = {},
            onNavigateToSettings = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
