package com.seucaio.unideas.feature.home.features.browse.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.screen.components.FiltersV2
import com.seucaio.unideas.feature.home.features.panel.screen.components.TasksNotesTabRowV2
import com.seucaio.unideas.feature.home.features.panel.screen.components.toListItemUi
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * New screen (#84, user-directed extrapolation beyond the original visual-swap scope): the same
 * Tasks/Notes tab + filters + list [HomeScreenV2] shows, but full-screen — no Priorities panel,
 * easier to browse the complete list. Reuses [HomeViewModel]/[HomeUiState]/[HomeEvent]/
 * [HomeUiAction] as-is (same data need, just a different presentation), rather than a new
 * ViewModel/use-case stack for what's the same underlying data. Reached from [HomeScreenV2]'s
 * TopBar action; [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen] (V1) is
 * untouched and has no entry point into this screen.
 */
@Composable
fun BrowseScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun BrowseContent(
    uiState: HomeUiState,
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
        BrowseBody(uiState = uiState, padding = padding, onEvent = onEvent)
    }
}

@Composable
private fun BrowseBody(
    uiState: HomeUiState,
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
            BrowseSuccessBody(state = uiState, modifier = Modifier.padding(padding), onEvent = onEvent)
    }
}

@Composable
private fun BrowseSuccessBody(
    state: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TasksNotesTabRowV2(
            activeTab = state.activeTab,
            onTabSelect = { onEvent(HomeEvent.OnTabChanged(it)) },
        )
        HorizontalDivider()
        FiltersV2(
            sections = state.availableSections,
            tags = state.availableTags,
            sectionFilter = state.sectionFilter,
            tagFilters = state.tagFilters,
            onSectionFilterChange = { onEvent(HomeEvent.OnSectionFilterChanged(it)) },
            onTagFilterToggle = { onEvent(HomeEvent.OnTagFilterToggled(it)) },
        )
        if (state.tabItems.isEmpty()) {
            val emptyMessageRes = if (state.hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding
            UnideasEmptyContent(messageRes = emptyMessageRes, modifier = Modifier.fillMaxSize())
        } else {
            val checkContentDescription = stringResource(R.string.home_item_recurring_content_description)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.tabItems, key = { it.id }) { item ->
                    ListItemRow(
                        ui = item.toListItemUi(checkContentDescription),
                        onClick = { onEvent(HomeEvent.OnItemClicked(item.id)) },
                        onToggleCheck = { onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        )
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BrowseScreenPreview(
    @PreviewParameter(HomePreviewProvider::class) uiState: HomeUiState,
) {
    UdsTheme {
        BrowseContent(
            uiState = uiState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
