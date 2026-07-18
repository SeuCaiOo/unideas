package com.seucaio.unideas.feature.tags

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
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.EntityListItemWithMenu
import com.seucaio.unideas.ds.components.legacy.NameInputDialog
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.tags.viewmodel.TagsDialogState
import com.seucaio.unideas.feature.tags.viewmodel.TagsEvent
import com.seucaio.unideas.feature.tags.viewmodel.TagsUiAction
import com.seucaio.unideas.feature.tags.viewmodel.TagsUiState
import com.seucaio.unideas.feature.tags.viewmodel.TagsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * V1 — superseded by [TagsScreenV2] (#84). Kept only for the `DevScreenVersionToggle`
 * side-by-side comparison; scheduled for removal once V2 is confirmed and the epic branch
 * merges. Don't add new behavior here — any fix belongs in V2 too (or V2-only, if the fix is
 * about something V1 no longer does, like the Add/Rename dialogs V2 replaced).
 */
@Deprecated(
    "Superseded by TagsScreenV2 (#84) — kept only for the dev toggle comparison.",
    ReplaceWith("TagsScreenV2(onNavigateBack)"),
)
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
            ConditionalFab(visible = uiState is TagsUiState.Success) {
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
    EntityListItemWithMenu(
        title = tag.name,
        optionsContentDescription = stringResource(R.string.tag_options),
        renameLabel = stringResource(R.string.tag_rename_action),
        deleteLabel = stringResource(R.string.tag_delete_action),
        onRenameClick = { onEvent(TagsEvent.OnRenameClicked(tag)) },
        onDeleteClick = { onEvent(TagsEvent.OnDeleteClicked(tag)) },
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
    UdsTheme {
        TagsContent(
            uiState = previewState.uiState,
            dialogState = previewState.dialogState,
            onEvent = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
