package com.seucaio.unideas.feature.sections

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
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.feature.sections.viewmodel.SectionsViewModel
import org.koin.androidx.compose.koinViewModel

private val sectionsScreenStrings = EntityScreenStrings(
    title = R.string.sections_title,
    addLabel = R.string.sections_add,
    addFieldLabel = R.string.sections_add_label,
    renameLabel = R.string.sections_rename,
    renameFieldLabel = R.string.sections_rename_label,
    emptyMessage = R.string.sections_empty,
    optionsDescription = R.string.section_options,
    renameAction = R.string.section_rename_action,
    deleteAction = R.string.section_delete_action,
    deleteConfirmTitle = R.string.section_delete_confirm_title,
    deleteConfirmMessage = R.string.section_delete_confirm_message,
)

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
                is EntityUiAction.ShowSnackbar ->
                    resources.getString(action.messageRes, *action.formatArgs.toTypedArray())
                is EntityUiAction.ShowError -> action.message
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

@Composable
private fun SectionsContent(
    uiState: EntityCrudUiState<Section>,
    dialogState: EntityDialogState<Section>,
    onEvent: (EntityEvent<Section>) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    EntityManagementScreen(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        strings = sectionsScreenStrings,
        itemLabel = { it.name },
        itemKey = { it.id },
    )
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
