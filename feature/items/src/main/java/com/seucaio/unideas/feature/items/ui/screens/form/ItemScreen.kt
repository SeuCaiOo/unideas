package com.seucaio.unideas.feature.items.ui.screens.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.extensions.shareText
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.ItemActions
import com.seucaio.unideas.feature.items.ui.components.ItemFormBody
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormDialogState
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormUiAction
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormUiState
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * #86 Pacote 2, direção V4: a tela nasce **sempre editável** — não existe nenhum estado de
 * leitura, nem um botão de "editar" para sair dele (ao contrário de [ItemFormSheet]/
 * [ItemFormScreenV3], que alternam entre um estado read-only e um editável). O real ponto de
 * comparação dessa direção é de navegação, não visual: tocar num item levaria direto pra cá,
 * pulando [com.seucaio.unideas.feature.items.features.detail.screen.ItemDetailScreen] por
 * completo — decisão de rota ainda adiada (#86), então essa tela por si só acaba ficando parecida
 * com [ItemFormScreen] hoje (o mesmo corpo de campos), só sem a distinção de título
 * criar/editar, já que aqui não faz sentido diferenciar os dois — é sempre "editando". Reaproveita
 * [ItemFormViewModel]/[ItemFormUiState]/[ItemFormEvent] como estão. Sem rota nova no nav graph
 * ainda — visível só via `@PreviewLightDark`.
 */
@Composable
fun ItemScreen(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemFormUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is ItemFormUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemFormUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
                is ItemFormUiAction.ShareText -> context.shareText(action.text)
            }
        }
    }

    ItemScreenContent(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemScreenContent(
    uiState: ItemFormUiState,
    dialogState: ItemFormDialogState,
    onEvent: (ItemFormEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val fieldsEvents = remember(onEvent) {
        ItemFormFieldsEvents(
            onTypeChanged = { onEvent(ItemFormEvent.OnTypeChanged(it)) },
            onTitleChanged = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
            onDescriptionChanged = { onEvent(ItemFormEvent.OnDescriptionChanged(it)) },
            onSectionChanged = { onEvent(ItemFormEvent.OnSectionChanged(it)) },
            onTagToggled = { onEvent(ItemFormEvent.OnTagToggled(it)) },
            onDueDateChanged = { onEvent(ItemFormEvent.OnDueDateChanged(it)) },
            onRecurrenceChanged = { onEvent(ItemFormEvent.OnRecurrenceChanged(it)) },
            onSaveClicked = { onEvent(ItemFormEvent.OnSaveClicked) },
        )
    }

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.item_form_title_view) + " — V4",
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    ItemActions(
                        canComplete = uiState.typeIsTask && !uiState.isCompleted,
                        onShareClicked = { onEvent(ItemFormEvent.OnShareClicked) },
                        onDeleteClicked = { onEvent(ItemFormEvent.OnDeleteClicked) },
                        onCompleteClicked = { onEvent(ItemFormEvent.OnCompleteClicked) },
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.isLoading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
            uiState.loadFailed -> UnideasErrorContent(
                messageRes = R.string.item_form_load_error,
                onRetry = { onEvent(ItemFormEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
            else -> ItemFormBody(
                state = uiState,
                events = fieldsEvents,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (dialogState is ItemFormDialogState.DeleteConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.item_detail_delete_title,
            messageRes = R.string.item_detail_delete_message,
            onDismiss = { onEvent(ItemFormEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemFormEvent.OnDeleteConfirmClicked) },
        )
    }
}

@PreviewLightDark
@Composable
private fun ItemScreenPreview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        ItemScreenContent(
            uiState = previewState.uiState,
            dialogState = ItemFormDialogState.None,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
