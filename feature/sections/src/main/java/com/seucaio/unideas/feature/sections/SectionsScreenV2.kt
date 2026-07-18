package com.seucaio.unideas.feature.sections

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
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.ds.components.inputs.AddEntryRow
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.EntityListItemWithMenu
import com.seucaio.unideas.ds.components.legacy.NameInputDialog
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.sections.viewmodel.SectionsDialogState
import com.seucaio.unideas.feature.sections.viewmodel.SectionsEvent
import com.seucaio.unideas.feature.sections.viewmodel.SectionsUiAction
import com.seucaio.unideas.feature.sections.viewmodel.SectionsUiState
import com.seucaio.unideas.feature.sections.viewmodel.SectionsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * V2 (#84): same [SectionsViewModel]/[SectionsUiState]/[SectionsEvent]/[SectionsDialogState]
 * contract as [SectionsScreen] — visual pass only. Row keeps the legacy
 * [EntityListItemWithMenu] (kebab + rename/delete) since `:uds`'s native `ManageListRow` requires
 * a subtitle Section has no data for (item count isn't tracked — out of scope, visual-only pass).
 * Add moves from a dialog to `:uds`'s native [AddEntryRow], always visible above the list, per the
 * PDF reference — a pure screen-side swap: it still calls the same
 * [SectionsEvent.OnAddConfirmClicked], [SectionsDialogState.Add] just never gets triggered from
 * here. Rename/Delete keep their dialogs (no native inline-edit equivalent exists yet).
 */
@Composable
fun SectionsScreenV2(
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

    SectionsContentV2(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun SectionsContentV2(
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        SectionsBodyV2(uiState = uiState, padding = padding, onEvent = onEvent)
    }

    SectionsDialogsV2(dialogState = dialogState, onEvent = onEvent)
}

@Composable
private fun SectionsBodyV2(
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
        is SectionsUiState.Success ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                var newSectionName by remember { mutableStateOf("") }
                AddEntryRow(
                    value = newSectionName,
                    onValueChange = { newSectionName = it },
                    placeholder = stringResource(R.string.sections_add_label),
                    addContentDescription = stringResource(R.string.sections_add),
                    onSubmit = {
                        if (newSectionName.isNotBlank()) {
                            onEvent(SectionsEvent.OnAddConfirmClicked(newSectionName))
                            newSectionName = ""
                        }
                    },
                )
                if (uiState.sections.isEmpty()) {
                    UnideasEmptyContent(messageRes = R.string.sections_empty, modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.sections, key = { it.id }) { section ->
                            SectionRowV2(section = section, onEvent = onEvent)
                        }
                    }
                }
            }
    }
}

@Composable
private fun SectionRowV2(
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
private fun SectionsDialogsV2(
    dialogState: SectionsDialogState,
    onEvent: (SectionsEvent) -> Unit,
) {
    when (dialogState) {
        is SectionsDialogState.None, is SectionsDialogState.Add -> Unit
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
private fun SectionsScreenV2Preview(
    @PreviewParameter(SectionsPreviewProvider::class) previewState: SectionsPreviewState,
) {
    UdsTheme {
        SectionsContentV2(
            uiState = previewState.uiState,
            dialogState = previewState.dialogState,
            onEvent = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
