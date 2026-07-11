package com.seucaio.unideas.feature.tags

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
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.tags.viewmodel.TagsDialogState
import com.seucaio.unideas.feature.tags.viewmodel.TagsEvent
import com.seucaio.unideas.feature.tags.viewmodel.TagsUiAction
import com.seucaio.unideas.feature.tags.viewmodel.TagsUiState
import com.seucaio.unideas.feature.tags.viewmodel.TagsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TagsScreen(
    onNavigateBack: (() -> Unit)?,
    viewModel: TagsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            val message = when (action) {
                is TagsUiAction.ShowSnackbar ->
                    resources.getString(action.messageRes, *action.formatArgs.toTypedArray())
                is TagsUiAction.ShowError -> action.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    TagsContent(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsContent(
    uiState: TagsUiState,
    dialogState: TagsDialogState,
    onEvent: (TagsEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(title = stringResource(R.string.tags_title), onNavigateBack = updatedOnNavigateBack)
        },
        floatingActionButton = {
            // FAB only once we have a definitive answer (empty or with data) — not while
            // loading or errored, since there's nothing to add a tag to yet.
            if (uiState is TagsUiState.Success) {
                FloatingActionButton(onClick = { onEvent(TagsEvent.OnAddClicked) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tags_add))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        TagsBody(uiState = uiState, padding = padding, onEvent = onEvent)
    }

    TagsDialogs(dialogState = dialogState, onEvent = onEvent)
}

@Composable
private fun TagsBody(
    uiState: TagsUiState,
    padding: PaddingValues,
    onEvent: (TagsEvent) -> Unit,
) {
    when (uiState) {
        is TagsUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
        is TagsUiState.Error ->
            UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(TagsEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
        is TagsUiState.Success -> {
            if (uiState.tags.isEmpty()) {
                UnideasEmptyContent(messageRes = R.string.tags_empty, modifier = Modifier.padding(padding))
            } else {
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    items(uiState.tags, key = { it.id }) { tag ->
                        TagRow(tag = tag, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun TagRow(
    tag: Tag,
    onEvent: (TagsEvent) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    UnideasListItem(
        title = tag.name,
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.tag_options))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.tag_rename_action)) },
                        onClick = {
                            menuExpanded = false
                            onEvent(TagsEvent.OnRenameClicked(tag))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.tag_delete_action)) },
                        onClick = {
                            menuExpanded = false
                            onEvent(TagsEvent.OnDeleteClicked(tag))
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun TagsDialogs(
    dialogState: TagsDialogState,
    onEvent: (TagsEvent) -> Unit,
) {
    when (dialogState) {
        is TagsDialogState.None -> Unit
        is TagsDialogState.Add ->
            NameInputDialog(
                title = stringResource(R.string.tags_add),
                label = stringResource(R.string.tags_add_label),
                onConfirm = { name -> onEvent(TagsEvent.OnAddConfirmClicked(name)) },
                onDismiss = { onEvent(TagsEvent.OnDialogDismissed) },
            )
        is TagsDialogState.Rename ->
            NameInputDialog(
                title = stringResource(R.string.tags_rename),
                label = stringResource(R.string.tags_rename_label),
                initialValue = dialogState.tag.name,
                onConfirm = { newName -> onEvent(TagsEvent.OnRenameConfirmClicked(newName)) },
                onDismiss = { onEvent(TagsEvent.OnDialogDismissed) },
            )
        is TagsDialogState.Delete ->
            DeleteConfirmationDialog(
                titleRes = R.string.tag_delete_confirm_title,
                messageRes = R.string.tag_delete_confirm_message,
                onDismiss = { onEvent(TagsEvent.OnDialogDismissed) },
                onConfirm = { onEvent(TagsEvent.OnDeleteConfirmClicked) },
            )
    }
}

@PreviewLightDark
@Composable
private fun TagsScreenPreview(
    @PreviewParameter(TagsPreviewProvider::class) previewState: TagsPreviewState,
) {
    UnideasTheme {
        TagsContent(
            uiState = previewState.uiState,
            dialogState = previewState.dialogState,
            onEvent = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
