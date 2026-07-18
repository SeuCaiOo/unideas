package com.seucaio.unideas.feature.tags

import androidx.compose.foundation.layout.Column
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
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.ds.components.inputs.AddEntryRow
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
 * V2 (#84): same [TagsViewModel]/[TagsUiState]/[TagsEvent]/[TagsDialogState] contract as
 * [TagsScreen] — visual pass only. Mirrors
 * [com.seucaio.unideas.feature.sections.SectionsScreenV2]'s decisions (same layout, same
 * `EntityListItemWithMenu`/`AddEntryRow` tradeoffs — see that file's kdoc for the "why").
 */
@Composable
fun TagsScreenV2(
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

    TagsContentV2(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun TagsContentV2(
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        TagsBodyV2(uiState = uiState, padding = padding, onEvent = onEvent)
    }

    TagsDialogsV2(dialogState = dialogState, onEvent = onEvent)
}

@Composable
private fun TagsBodyV2(
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
        is TagsUiState.Success ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                var newTagName by remember { mutableStateOf("") }
                AddEntryRow(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    placeholder = stringResource(R.string.tags_add_label),
                    addContentDescription = stringResource(R.string.tags_add),
                    onSubmit = {
                        if (newTagName.isNotBlank()) {
                            onEvent(TagsEvent.OnAddConfirmClicked(newTagName))
                            newTagName = ""
                        }
                    },
                )
                if (uiState.tags.isEmpty()) {
                    UnideasEmptyContent(messageRes = R.string.tags_empty, modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.tags, key = { it.id }) { tag ->
                            TagRowV2(tag = tag, onEvent = onEvent)
                        }
                    }
                }
            }
    }
}

@Composable
private fun TagRowV2(
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
private fun TagsDialogsV2(
    dialogState: TagsDialogState,
    onEvent: (TagsEvent) -> Unit,
) {
    when (dialogState) {
        is TagsDialogState.None, is TagsDialogState.Add -> Unit
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
private fun TagsScreenV2Preview(
    @PreviewParameter(TagsPreviewProvider::class) previewState: TagsPreviewState,
) {
    UdsTheme {
        TagsContentV2(
            uiState = previewState.uiState,
            dialogState = previewState.dialogState,
            onEvent = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
