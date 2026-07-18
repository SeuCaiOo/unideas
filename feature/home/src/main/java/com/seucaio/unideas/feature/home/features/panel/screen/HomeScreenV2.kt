package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiAction
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenV2(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
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
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
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
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.home_settings_action))
                    }
                },
            )
        },
        floatingActionButton = {
            ConditionalFab(visible = uiState is HomeUiState.Success) {
                Box {
                    FloatingActionButton(onClick = { addMenuExpanded = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.home_add_action))
                    }
                    DropdownMenu(expanded = addMenuExpanded, onDismissRequest = { addMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_add_task)) },
                            onClick = {
                                addMenuExpanded = false
                                onEvent(HomeEvent.OnAddClicked(ItemType.TASK))
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_add_note)) },
                            onClick = {
                                addMenuExpanded = false
                                onEvent(HomeEvent.OnAddClicked(ItemType.NOTE))
                            },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeBody(uiState = uiState, padding = padding, onEvent = onEvent)
    }
}

@Composable
private fun HomeBody(
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
            HomeSuccessBody(state = uiState, modifier = Modifier.padding(padding), onEvent = onEvent)
    }
}

@Composable
private fun HomeSuccessBody(
    state: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {}
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomePreviewProvider::class) uiState: HomeUiState,
) {
    UdsTheme {
        HomeContent(
            uiState = uiState,
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
