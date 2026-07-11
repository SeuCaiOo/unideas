package com.seucaio.unideas.feature.tags

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.crud.EntityCrudUiState
import com.seucaio.unideas.core.common.crud.EntityDialogState
import com.seucaio.unideas.core.common.crud.EntityEvent
import com.seucaio.unideas.core.common.crud.EntityUiAction
import com.seucaio.unideas.core.ui.components.EntityManagementScreen
import com.seucaio.unideas.core.ui.components.EntityScreenStrings
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.tags.viewmodel.TagsViewModel
import org.koin.androidx.compose.koinViewModel

private val tagsScreenStrings = EntityScreenStrings(
    title = R.string.tags_title,
    addLabel = R.string.tags_add,
    addFieldLabel = R.string.tags_add_label,
    renameLabel = R.string.tags_rename,
    renameFieldLabel = R.string.tags_rename_label,
    emptyMessage = R.string.tags_empty,
    optionsDescription = R.string.tag_options,
    renameAction = R.string.tag_rename_action,
    deleteAction = R.string.tag_delete_action,
    deleteConfirmTitle = R.string.tag_delete_confirm_title,
    deleteConfirmMessage = R.string.tag_delete_confirm_message,
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
                is EntityUiAction.ShowSnackbar ->
                    resources.getString(action.messageRes, *action.formatArgs.toTypedArray())
                is EntityUiAction.ShowError -> action.message
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

@Composable
private fun TagsContent(
    uiState: EntityCrudUiState<Tag>,
    dialogState: EntityDialogState<Tag>,
    onEvent: (EntityEvent<Tag>) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    EntityManagementScreen(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        strings = tagsScreenStrings,
        itemLabel = { it.name },
        itemKey = { it.id },
    )
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
