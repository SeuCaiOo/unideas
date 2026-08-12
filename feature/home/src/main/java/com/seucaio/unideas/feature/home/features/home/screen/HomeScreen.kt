package com.seucaio.unideas.feature.home.features.home.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.theme.UdsTheme
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

    LaunchedEffect(Unit) {
        if (!ColdStartPriorityPrompt.shown) {
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
                onNavigateBack = updatedOnNavigateBack,
                onShowPriorities = { showPriorityBottomSheet = true },
                onNavigateToSettings = onNavigateToSettings,
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
            padding = padding,
            onEvent = onEvent,
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
            onTagFilterToggle = { onEvent(HomeEvent.OnTagFilterToggled(it)) }
        )
        ItemsContent(
            itemsState = itemsState,
            sectionFilter = filterState.sectionFilter,
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
