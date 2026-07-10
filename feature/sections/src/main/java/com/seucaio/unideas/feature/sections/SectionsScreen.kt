package com.seucaio.unideas.feature.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.ui.components.DeleteConfirmationDialog
import com.seucaio.unideas.core.ui.components.NameInputDialog
import com.seucaio.unideas.core.ui.components.UnideasEmptyContent
import com.seucaio.unideas.core.ui.components.UnideasErrorContent
import com.seucaio.unideas.core.ui.components.UnideasListItem
import com.seucaio.unideas.core.ui.components.UnideasLoadingContent
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Section
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
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
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
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionsContent(
    uiState: SectionsUiState,
    onEvent: (SectionsEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    var showAddDialog by remember { mutableStateOf(false) }
    var sectionToRename by remember { mutableStateOf<Section?>(null) }
    var sectionToDelete by remember { mutableStateOf<Section?>(null) }

    Scaffold(
        topBar = {
            UnideasTopBar(title = stringResource(R.string.sections_title), onNavigateBack = updatedOnNavigateBack)
        },
        floatingActionButton = {
            // FAB only once we have a definitive answer (empty or with data) — not while
            // loading or errored, since there's nothing to add a section to yet.
            if (uiState is SectionsUiState.Success) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.sections_add))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        SectionsBody(
            uiState = uiState,
            padding = padding,
            onEvent = onEvent,
            onRenameClick = { sectionToRename = it },
            onDeleteClick = { sectionToDelete = it },
        )
    }

    SectionsDialogs(
        showAddDialog = showAddDialog,
        onAddDismiss = { showAddDialog = false },
        onAddConfirm = { name ->
            onEvent(SectionsEvent.OnAddClicked(name))
            showAddDialog = false
        },
        sectionToRename = sectionToRename,
        onRenameDismiss = { sectionToRename = null },
        onRenameConfirm = { section, newName ->
            onEvent(SectionsEvent.OnRenameClicked(section, newName))
            sectionToRename = null
        },
        sectionToDelete = sectionToDelete,
        onDeleteDismiss = { sectionToDelete = null },
        onDeleteConfirm = { section ->
            onEvent(SectionsEvent.OnDeleteClicked(section.id))
            sectionToDelete = null
        },
    )
}

@Composable
private fun SectionsBody(
    uiState: SectionsUiState,
    padding: PaddingValues,
    onEvent: (SectionsEvent) -> Unit,
    onRenameClick: (Section) -> Unit,
    onDeleteClick: (Section) -> Unit,
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
                        SectionRow(section = section, onRenameClick = onRenameClick, onDeleteClick = onDeleteClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRow(
    section: Section,
    onRenameClick: (Section) -> Unit,
    onDeleteClick: (Section) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    UnideasListItem(
        title = section.name,
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.section_options))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.section_rename_action)) },
                        onClick = {
                            menuExpanded = false
                            onRenameClick(section)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.section_delete_action)) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick(section)
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun SectionsDialogs(
    showAddDialog: Boolean,
    onAddDismiss: () -> Unit,
    onAddConfirm: (String) -> Unit,
    sectionToRename: Section?,
    onRenameDismiss: () -> Unit,
    onRenameConfirm: (Section, String) -> Unit,
    sectionToDelete: Section?,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: (Section) -> Unit,
) {
    if (showAddDialog) {
        NameInputDialog(
            title = stringResource(R.string.sections_add),
            label = stringResource(R.string.sections_add_label),
            onConfirm = onAddConfirm,
            onDismiss = onAddDismiss,
        )
    }

    sectionToRename?.let { section ->
        NameInputDialog(
            title = stringResource(R.string.sections_rename),
            label = stringResource(R.string.sections_rename_label),
            initialValue = section.name,
            onConfirm = { newName -> onRenameConfirm(section, newName) },
            onDismiss = onRenameDismiss,
        )
    }

    sectionToDelete?.let { section ->
        DeleteConfirmationDialog(
            titleRes = R.string.section_delete_confirm_title,
            messageRes = R.string.section_delete_confirm_message,
            onDismiss = onDeleteDismiss,
            onConfirm = { onDeleteConfirm(section) },
        )
    }
}

@PreviewLightDark
@Composable
private fun SectionsScreenPreview(
    @PreviewParameter(SectionsPreviewProvider::class) uiState: SectionsUiState,
) {
    UnideasTheme {
        SectionsContent(
            uiState = uiState,
            onEvent = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
