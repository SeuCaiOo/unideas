package com.seucaio.unideas.feature.sections

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.ui.components.ConditionalFab
import com.seucaio.unideas.core.ui.components.DeleteConfirmationDialog
import com.seucaio.unideas.core.ui.components.EntityListItemWithMenu
import com.seucaio.unideas.core.ui.components.NameInputDialog
import com.seucaio.unideas.core.ui.components.UnideasEmptyContent
import com.seucaio.unideas.core.ui.components.UnideasErrorContent
import com.seucaio.unideas.core.ui.components.UnideasLoadingContent
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.feature.sections.viewmodel.SectionsDialogState
import com.seucaio.unideas.feature.sections.viewmodel.SectionsEvent
import com.seucaio.unideas.feature.sections.viewmodel.SectionsUiAction
import com.seucaio.unideas.feature.sections.viewmodel.SectionsUiState
import com.seucaio.unideas.feature.sections.viewmodel.SectionsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SectionsScreen(
    onNavigateBack: (() -> Unit)?,
    viewModel: SectionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            val message = when (action) {
                is SectionsUiAction.ShowSnackbar ->
                    resources.getString(action.messageRes, *action.formatArgs.toTypedArray())
                is SectionsUiAction.ShowError -> action.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    SectionsContent(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionsContent(
    uiState: SectionsUiState,
    dialogState: SectionsDialogState,
    onEvent: (SectionsEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(title = stringResource(R.string.sections_title), onNavigateBack = updatedOnNavigateBack)
        },
        floatingActionButton = {
            ConditionalFab(visible = uiState is SectionsUiState.Success) {
                FloatingActionButton(onClick = { onEvent(SectionsEvent.OnAddClicked) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.sections_add))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        SectionsBody(uiState = uiState, padding = padding, onEvent = onEvent)
    }

    SectionsDialogs(dialogState = dialogState, onEvent = onEvent)
}

@Composable
private fun SectionsBody(
    uiState: SectionsUiState,
    padding: PaddingValues,
    onEvent: (SectionsEvent) -> Unit,
) {
    when (uiState) {
        is SectionsUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
        is SectionsUiState.Error ->
            UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(SectionsEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
        is SectionsUiState.Success -> {
            if (uiState.sections.isEmpty()) {
                UnideasEmptyContent(messageRes = R.string.sections_empty, modifier = Modifier.padding(padding))
            } else {
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    items(uiState.sections, key = { it.id }) { section ->
                        SectionRow(section = section, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRow(
    section: Section,
    onEvent: (SectionsEvent) -> Unit,
) {
    EntityListItemWithMenu(
        title = section.name,
        optionsContentDescription = stringResource(R.string.section_options),
        renameLabel = stringResource(R.string.section_rename_action),
        deleteLabel = stringResource(R.string.section_delete_action),
        onRenameClick = { onEvent(SectionsEvent.OnRenameClicked(section)) },
        onDeleteClick = { onEvent(SectionsEvent.OnDeleteClicked(section)) },
    )
}

@Composable
private fun SectionsDialogs(
    dialogState: SectionsDialogState,
    onEvent: (SectionsEvent) -> Unit,
) {
    when (dialogState) {
        is SectionsDialogState.None -> Unit
        is SectionsDialogState.Add ->
            NameInputDialog(
                title = stringResource(R.string.sections_add),
                label = stringResource(R.string.sections_add_label),
                onConfirm = { name -> onEvent(SectionsEvent.OnAddConfirmClicked(name)) },
                onDismiss = { onEvent(SectionsEvent.OnDialogDismissed) },
            )
        is SectionsDialogState.Rename ->
            NameInputDialog(
                title = stringResource(R.string.sections_rename),
                label = stringResource(R.string.sections_rename_label),
                initialValue = dialogState.section.name,
                onConfirm = { newName -> onEvent(SectionsEvent.OnRenameConfirmClicked(newName)) },
                onDismiss = { onEvent(SectionsEvent.OnDialogDismissed) },
            )
        is SectionsDialogState.Delete ->
            DeleteConfirmationDialog(
                titleRes = R.string.section_delete_confirm_title,
                messageRes = R.string.section_delete_confirm_message,
                onDismiss = { onEvent(SectionsEvent.OnDialogDismissed) },
                onConfirm = { onEvent(SectionsEvent.OnDeleteConfirmClicked) },
            )
    }
}

@PreviewLightDark
@Composable
private fun SectionsScreenPreview(
    @PreviewParameter(SectionsPreviewProvider::class) previewState: SectionsPreviewState,
) {
    UnideasTheme {
        SectionsContent(
            uiState = previewState.uiState,
            dialogState = previewState.dialogState,
            onEvent = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
