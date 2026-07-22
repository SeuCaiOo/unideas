package com.seucaio.unideas.feature.home.features.browse.screen

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewFixture
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.screen.components.ItemsContent
import com.seucaio.unideas.feature.home.features.panel.screen.components.ItemsFiltersBar
import com.seucaio.unideas.feature.home.features.panel.screen.components.TasksNotesTabRow
import com.seucaio.unideas.feature.home.features.panel.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsState
import org.koin.androidx.compose.koinViewModel

/**
 * New screen (#84, user-directed extrapolation beyond the original visual-swap scope): the same
 * Tasks/Notes tab + filters + list [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen]
 * shows, but full-screen — no Priorities panel, easier to browse the complete list. Reuses
 * [HomeViewModel]/[HomeEvent]/[HomeUiAction] as-is (same data need, just a different
 * presentation), rather than a new ViewModel/use-case stack for what's the same underlying data.
 * Collects [HomeViewModel.filterState]/[HomeViewModel.itemsState]/[HomeViewModel.uiState]
 * independently (#102, 2026-07-22), same as [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen] —
 * there's just no priority panel here to worry about. Reached from Home's TopBar action.
 */
@Composable
fun BrowseScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val updatedOnNavigateToForm by rememberUpdatedState(onNavigateToForm)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HomeUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is HomeUiAction.NavigateToForm -> updatedOnNavigateToForm(action.type)
                is HomeUiAction.NavigateToAllPriorities -> Unit
                is HomeUiAction.NavigateToSettings -> Unit
                is HomeUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    BrowseContent(
        uiState = uiState,
        filterState = filterState,
        itemsState = itemsState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun BrowseContent(
    uiState: HomeUiState,
    filterState: FilterState,
    itemsState: ItemsState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(title = stringResource(R.string.browse_title), onNavigateBack = updatedOnNavigateBack)
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
    uiState: HomeUiState,
    filterState: FilterState,
    itemsState: ItemsState,
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
    itemsState: ItemsState,
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
        )
    }
}

@PreviewLightDark
@Composable
private fun BrowseScreenPreview(
    @PreviewParameter(HomePreviewProvider::class) fixture: HomePreviewFixture,
) {
    UdsTheme {
        BrowseContent(
            uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem),
            filterState = fixture.filterState,
            itemsState = fixture.itemsState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
