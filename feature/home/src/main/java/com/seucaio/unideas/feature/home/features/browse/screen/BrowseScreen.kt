package com.seucaio.unideas.feature.home.features.browse.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.browse.screen.components.ItemsContent
import com.seucaio.unideas.feature.home.features.browse.screen.components.ItemsFiltersBar
import com.seucaio.unideas.feature.home.features.browse.screen.components.TasksNotesTabRow
import com.seucaio.unideas.feature.home.features.browse.viewmodel.BrowseEvent
import com.seucaio.unideas.feature.home.features.browse.viewmodel.BrowseItemsState
import com.seucaio.unideas.feature.home.features.browse.viewmodel.BrowseUiAction
import com.seucaio.unideas.feature.home.features.browse.viewmodel.BrowseUiState
import com.seucaio.unideas.feature.home.features.browse.viewmodel.BrowseViewModel
import com.seucaio.unideas.feature.home.features.browse.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.panel.screen.PriorityBottomSheet
import com.seucaio.unideas.feature.home.features.panel.screen.components.AddItemFab
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrowseScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: BrowseViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToAddItem by rememberUpdatedState(onNavigateToAddItem)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)
    val updatedOnNavigateToSettings by rememberUpdatedState(onNavigateToSettings)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is BrowseUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is BrowseUiAction.NavigateToAddItem -> updatedOnNavigateToAddItem(action.type)
                is BrowseUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    BrowseContent(
        uiState = uiState,
        filterState = filterState,
        itemsState = itemsState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = updatedOnNavigateToDetail,
        onNavigateToAllPriorities = updatedOnNavigateToAllPriorities,
        onNavigateToSettings = updatedOnNavigateToSettings,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun BrowseContent(
    uiState: BrowseUiState,
    filterState: FilterState,
    itemsState: BrowseItemsState,
    onEvent: (BrowseEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    var addMenuExpanded by remember { mutableStateOf(false) }
    var showPriorityBottomSheet by rememberSaveable { mutableStateOf(false) }

    if (showPriorityBottomSheet) {
        PriorityBottomSheet(
            onDismiss = { showPriorityBottomSheet = false },
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
        )
    }

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.browse_title),
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    IconButton(onClick = { showPriorityBottomSheet = true }) {
                        Icon(
                            Icons.Outlined.Flag,
                            contentDescription = stringResource(R.string.home_panel_title),
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.home_settings_action),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ConditionalFab(visible = uiState is BrowseUiState.Success) {
                AddItemFab(
                    expanded = addMenuExpanded,
                    onToggle = { addMenuExpanded = !addMenuExpanded },
                    onAddTask = {
                        addMenuExpanded = false
                        onEvent(BrowseEvent.OnAddClicked(ItemType.TASK))
                    },
                    onAddNote = {
                        addMenuExpanded = false
                        onEvent(BrowseEvent.OnAddClicked(ItemType.NOTE))
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        BrowseBody(
            uiState = uiState,
            filterState = filterState,
            itemsState = itemsState,
            padding = padding,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun BrowseBody(
    uiState: BrowseUiState,
    filterState: FilterState,
    itemsState: BrowseItemsState,
    padding: PaddingValues,
    onEvent: (BrowseEvent) -> Unit,
) {
    when (uiState) {
        is BrowseUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
        is BrowseUiState.Error ->
            UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(BrowseEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
        is BrowseUiState.Success ->
            BrowseSuccessBody(
                hasAnyItem = uiState.hasAnyItem,
                filterState = filterState,
                itemsState = itemsState,
                modifier = Modifier.padding(padding),
                onEvent = onEvent,
            )
    }
}

@Composable
private fun BrowseSuccessBody(
    hasAnyItem: Boolean,
    filterState: FilterState,
    itemsState: BrowseItemsState,
    onEvent: (BrowseEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TasksNotesTabRow(
            activeTab = filterState.activeTab,
            onTabSelect = { onEvent(BrowseEvent.OnTabChanged(it)) },
        )
        HorizontalDivider()
        ItemsFiltersBar(
            sections = filterState.availableSections,
            tags = filterState.availableTags,
            sectionFilter = filterState.sectionFilter,
            tagFilters = filterState.tagFilters,
            onSectionFilterChange = { onEvent(BrowseEvent.OnSectionFilterChanged(it)) },
            onTagFilterToggle = { onEvent(BrowseEvent.OnTagFilterToggled(it)) },
            viewMode = filterState.viewMode,
            onViewModeChange = { onEvent(BrowseEvent.OnViewModeChanged(it)) },
        )
        ItemsContent(
            itemsState = itemsState,
            filterState = filterState,
            hasAnyItem = hasAnyItem,
            onEvent = onEvent,
        )
    }
}

@PreviewLightDark
@Composable
private fun BrowseScreenPreview(
    @PreviewParameter(BrowsePreviewProvider::class) fixture: BrowsePreviewFixture,
) {
    UdsTheme {
        BrowseContent(
            uiState = BrowseUiState.Success(hasAnyItem = fixture.hasAnyItem),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            onEvent = {},
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAllPriorities = {},
            onNavigateToSettings = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
