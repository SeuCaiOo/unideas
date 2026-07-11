package com.seucaio.unideas.core.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.core.common.crud.EntityCrudUiState
import com.seucaio.unideas.core.common.crud.EntityDialogState
import com.seucaio.unideas.core.common.crud.EntityEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EntityManagementScreen(
    uiState: EntityCrudUiState<T>,
    dialogState: EntityDialogState<T>,
    onEvent: (EntityEvent<T>) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
    strings: EntityScreenStrings,
    itemLabel: (T) -> String,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            UnideasTopBar(title = stringResource(strings.title), onNavigateBack = updatedOnNavigateBack)
        },
        floatingActionButton = {
            // FAB only once we have a definitive answer (empty or with data) — not while
            // loading or errored, since there's nothing to add an entity to yet.
            if (uiState is EntityCrudUiState.Success) {
                FloatingActionButton(onClick = { onEvent(EntityEvent.OnAddClicked) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(strings.addLabel))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        EntityManagementBody(
            uiState = uiState,
            padding = padding,
            onEvent = onEvent,
            strings = strings,
            itemLabel = itemLabel,
            itemKey = itemKey,
        )
    }

    EntityManagementDialogs(dialogState = dialogState, onEvent = onEvent, strings = strings, itemLabel = itemLabel)
}

@Composable
private fun <T> EntityManagementBody(
    uiState: EntityCrudUiState<T>,
    padding: PaddingValues,
    onEvent: (EntityEvent<T>) -> Unit,
    strings: EntityScreenStrings,
    itemLabel: (T) -> String,
    itemKey: (T) -> Any,
) {
    when (uiState) {
        is EntityCrudUiState.Loading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
        is EntityCrudUiState.Error ->
            UnideasErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onEvent(EntityEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
        is EntityCrudUiState.Success -> {
            if (uiState.items.isEmpty()) {
                UnideasEmptyContent(messageRes = strings.emptyMessage, modifier = Modifier.padding(padding))
            } else {
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    items(uiState.items, key = itemKey) { item ->
                        EntityRow(item = item, onEvent = onEvent, strings = strings, itemLabel = itemLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> EntityRow(
    item: T,
    onEvent: (EntityEvent<T>) -> Unit,
    strings: EntityScreenStrings,
    itemLabel: (T) -> String,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    UnideasListItem(
        title = itemLabel(item),
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(strings.optionsDescription))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(strings.renameAction)) },
                        onClick = {
                            menuExpanded = false
                            onEvent(EntityEvent.OnRenameClicked(item))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(strings.deleteAction)) },
                        onClick = {
                            menuExpanded = false
                            onEvent(EntityEvent.OnDeleteClicked(item))
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun <T> EntityManagementDialogs(
    dialogState: EntityDialogState<T>,
    onEvent: (EntityEvent<T>) -> Unit,
    strings: EntityScreenStrings,
    itemLabel: (T) -> String,
) {
    when (dialogState) {
        is EntityDialogState.None -> Unit
        is EntityDialogState.Add ->
            NameInputDialog(
                title = stringResource(strings.addLabel),
                label = stringResource(strings.addFieldLabel),
                onConfirm = { name -> onEvent(EntityEvent.OnAddConfirmClicked(name)) },
                onDismiss = { onEvent(EntityEvent.OnDialogDismissed) },
            )
        is EntityDialogState.Rename ->
            NameInputDialog(
                title = stringResource(strings.renameLabel),
                label = stringResource(strings.renameFieldLabel),
                initialValue = itemLabel(dialogState.item),
                onConfirm = { newName -> onEvent(EntityEvent.OnRenameConfirmClicked(newName)) },
                onDismiss = { onEvent(EntityEvent.OnDialogDismissed) },
            )
        is EntityDialogState.Delete ->
            DeleteConfirmationDialog(
                titleRes = strings.deleteConfirmTitle,
                messageRes = strings.deleteConfirmMessage,
                onDismiss = { onEvent(EntityEvent.OnDialogDismissed) },
                onConfirm = { onEvent(EntityEvent.OnDeleteConfirmClicked) },
            )
    }
}
