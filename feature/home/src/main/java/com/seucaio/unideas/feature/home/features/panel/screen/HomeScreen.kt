package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.buttons.AppFab
import com.seucaio.unideas.ds.components.buttons.MiniFabAction
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.components.panels.PriorityPanel
import com.seucaio.unideas.ds.components.panels.PriorityRowUi
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.components.ItemsContent
import com.seucaio.unideas.feature.home.features.panel.screen.components.ItemsFiltersBar
import com.seucaio.unideas.feature.home.features.panel.screen.components.TasksNotesTabRow
import com.seucaio.unideas.feature.home.features.panel.screen.components.dueBadgeColor
import com.seucaio.unideas.feature.home.features.panel.screen.components.dueBadgeLabel
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * `:uds` native, 1a-Tonal-styled components (`PriorityPanel`, `TabItem`, `AppFab` +
 * `MiniFabAction`, `ListItemRow`/`ListItemCard` via [ItemsContent], `FilterDropdownPill`/`SelectableChip` via
 * [ItemsFiltersBar]).
 */
@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBrowse: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToForm by rememberUpdatedState(onNavigateToForm)
    val updatedOnNavigateToAllPriorities by rememberUpdatedState(onNavigateToAllPriorities)
    val updatedOnNavigateToSettings by rememberUpdatedState(onNavigateToSettings)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HomeUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is HomeUiAction.NavigateToForm -> updatedOnNavigateToForm(action.type)
                is HomeUiAction.NavigateToAllPriorities -> updatedOnNavigateToAllPriorities()
                is HomeUiAction.NavigateToSettings -> updatedOnNavigateToSettings()
                is HomeUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToBrowse = onNavigateToBrowse,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToBrowse: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var addMenuExpanded by remember { mutableStateOf(false) }
    val updatedOnNavigateToBrowse by rememberUpdatedState(onNavigateToBrowse)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.home_title),
                actions = {
                    IconButton(onClick = { onEvent(HomeEvent.OnSettingsClicked) }) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.home_settings_action)
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ConditionalFab(visible = uiState is HomeUiState.Success) {
                HomeAddFab(
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeBody(
            uiState = uiState,
            padding = padding,
            onEvent = onEvent,
            onNavigateToBrowse = updatedOnNavigateToBrowse,
        )
    }
}

@Composable
private fun HomeAddFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddTask: () -> Unit,
    onAddNote: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            MiniFabAction(
                icon = Icons.AutoMirrored.Outlined.Notes,
                label = stringResource(R.string.home_add_note),
                onClick = onAddNote,
            )
            Spacer(Modifier.height(8.dp))
            MiniFabAction(
                icon = Icons.Outlined.TaskAlt,
                label = stringResource(R.string.home_add_task),
                onClick = onAddTask
            )
            Spacer(Modifier.height(12.dp))
        }
        AppFab(
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.home_add_action),
            onClick = onToggle
        )
    }
}

@Composable
private fun HomeBody(
    uiState: HomeUiState,
    padding: PaddingValues,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToBrowse: () -> Unit,
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
                state = uiState,
                modifier = Modifier.padding(padding),
                onEvent = onEvent,
                onNavigateToBrowse = onNavigateToBrowse,
            )
    }
}

@Composable
private fun HomeSuccessBody(
    state: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToBrowse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PriorityPanel(
            title = stringResource(R.string.home_panel_title),
            icon = Icons.Outlined.Flag,
            rows = state.priorityItems.map { it.toPriorityRowUi() },
            footerLabel = if (state.showSeeAllButton) stringResource(R.string.home_see_all) else null,
            onFooterClick = { onEvent(HomeEvent.OnSeeAllClicked) },
            onRowClick = { id -> onEvent(HomeEvent.OnItemClicked(id)) },
            emptyText = stringResource(R.string.home_panel_empty),
        )

        TasksNotesTabRow(
            activeTab = state.activeTab,
            onTabSelect = { onEvent(HomeEvent.OnTabChanged(it)) },
        )

        ItemsFiltersBar(
            sections = state.availableSections,
            tags = state.availableTags,
            sectionFilter = state.sectionFilter,
            tagFilters = state.tagFilters,
            onSectionFilterChange = { onEvent(HomeEvent.OnSectionFilterChanged(it)) },
            onTagFilterToggle = { onEvent(HomeEvent.OnTagFilterToggled(it)) },
            viewMode = state.viewMode,
            onViewModeChange = { onEvent(HomeEvent.OnViewModeChanged(it)) },
        )
        ItemsContent(
            state = state,
            onEvent = onEvent,
        ) {
            NavRow(
                icon = Icons.AutoMirrored.Outlined.List,
                label = stringResource(R.string.browse_action),
                onClick = onNavigateToBrowse,
            )
        }
    }
}

@Composable
private fun Item.toPriorityRowUi(): PriorityRowUi = PriorityRowUi(
    id = id,
    title = title,
    badgeLabel = dueBadgeLabel(this),
    badgeColor = dueBadgeColor(this),
)

@PreviewLightDark
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomePreviewProvider::class) uiState: HomeUiState,
) {
    UdsTheme {
        HomeContent(
            uiState = uiState,
            onEvent = {},
            onNavigateToBrowse = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
