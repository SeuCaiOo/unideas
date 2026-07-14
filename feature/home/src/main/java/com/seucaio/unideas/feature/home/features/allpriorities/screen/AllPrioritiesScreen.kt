package com.seucaio.unideas.feature.home.features.allpriorities.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.allpriorities.viewmodel.AllPrioritiesEvent
import com.seucaio.unideas.feature.home.features.allpriorities.viewmodel.AllPrioritiesUiAction
import com.seucaio.unideas.feature.home.features.allpriorities.viewmodel.AllPrioritiesUiState
import com.seucaio.unideas.feature.home.features.allpriorities.viewmodel.AllPrioritiesViewModel
import com.seucaio.unideas.feature.home.features.panel.screen.components.HomeItemRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllPrioritiesScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: AllPrioritiesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is AllPrioritiesUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is AllPrioritiesUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    AllPrioritiesContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun AllPrioritiesContent(
    uiState: AllPrioritiesUiState,
    onEvent: (AllPrioritiesEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.all_priorities_title),
                onNavigateBack = updatedOnNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        AllPrioritiesBody(uiState = uiState, padding = padding, onEvent = onEvent)
    }
}

@Composable
private fun AllPrioritiesBody(
    uiState: AllPrioritiesUiState,
    padding: PaddingValues,
    onEvent: (AllPrioritiesEvent) -> Unit,
) {
    when (uiState) {
        is AllPrioritiesUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
        is AllPrioritiesUiState.Error ->
            UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(AllPrioritiesEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
        is AllPrioritiesUiState.Success ->
            if (uiState.items.isEmpty()) {
                UnideasEmptyContent(
                    messageRes = R.string.all_priorities_empty,
                    modifier = Modifier.padding(padding).fillMaxSize(),
                )
            } else {
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    items(uiState.items, key = { it.id }) { item ->
                        HomeItemRow(
                            item = item,
                            onClick = { onEvent(AllPrioritiesEvent.OnItemClicked(item.id)) },
                            onComplete = { onEvent(AllPrioritiesEvent.OnCompleteClicked(item.id)) },
                        )
                    }
                }
            }
    }
}

@PreviewLightDark
@Composable
private fun AllPrioritiesScreenPreview(
    @PreviewParameter(AllPrioritiesPreviewProvider::class) uiState: AllPrioritiesUiState,
) {
    UdsTheme {
        AllPrioritiesContent(
            uiState = uiState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
