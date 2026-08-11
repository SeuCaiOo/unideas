package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.panels.PriorityPanel
import com.seucaio.unideas.ds.components.panels.PriorityRowUi
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.components.AddItemFab
import com.seucaio.unideas.feature.home.features.panel.screen.components.dueBadgeColor
import com.seucaio.unideas.feature.home.features.panel.screen.components.dueBadgeLabel
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
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
                is HomeUiAction.NavigateToAllPriorities -> updatedOnNavigateToAllPriorities()
                is HomeUiAction.NavigateToSettings -> updatedOnNavigateToSettings()
                is HomeUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    HomeContent(
        uiState = uiState,
        itemsState = itemsState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    itemsState: HomeItemsState,
    onEvent: (HomeEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var addMenuExpanded by remember { mutableStateOf(false) }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeBody(
            uiState = uiState,
            itemsState = itemsState,
            padding = padding,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun HomeBody(
    uiState: HomeUiState,
    itemsState: HomeItemsState,
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
                itemsState = itemsState,
                modifier = Modifier.padding(padding),
                onEvent = onEvent,
            )
    }
}

@Composable
private fun HomeSuccessBody(
    itemsState: HomeItemsState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PriorityPanel(
            title = stringResource(R.string.home_panel_title),
            icon = Icons.Outlined.Flag,
            rows = itemsState.priorityItems.map { it.toPriorityRowUi() },
            footerLabel = if (itemsState.showSeeAllButton) stringResource(R.string.home_see_all) else null,
            onFooterClick = { onEvent(HomeEvent.OnSeeAllClicked) },
            onRowClick = { id -> onEvent(HomeEvent.OnItemClicked(id)) },
            emptyText = stringResource(R.string.home_panel_empty),
            modifier = Modifier.fillMaxWidth(),
        )
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
    @PreviewParameter(HomePreviewProvider::class) fixture: HomePreviewFixture,
) {
    UdsTheme {
        HomeContent(
            uiState = HomeUiState.Success(hasAnyItem = fixture.hasAnyItem),
            itemsState = fixture.itemsState,
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
