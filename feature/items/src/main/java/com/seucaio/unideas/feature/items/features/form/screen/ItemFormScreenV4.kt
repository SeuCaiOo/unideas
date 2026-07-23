package com.seucaio.unideas.feature.items.features.form.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.inputs.AppTextField
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.screen.components.DueDateField
import com.seucaio.unideas.feature.items.features.form.screen.components.RecurrenceField
import com.seucaio.unideas.feature.items.features.form.screen.components.SectionField
import com.seucaio.unideas.feature.items.features.form.screen.components.TagsField
import com.seucaio.unideas.feature.items.features.form.screen.components.TypeSelectorField
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiAction
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiState
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * #86 Pacote 2, direção V4: a tela nasce **sempre editável** — não existe nenhum estado de
 * leitura, nem um botão de "editar" para sair dele (ao contrário de [ItemFormScreenV2]/
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
fun ItemFormScreenV4(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemFormUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is ItemFormUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemFormUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ItemFormV4Content(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormV4Content(
    uiState: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.item_form_title_view) + " — V4",
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    IconButton(
                        onClick = { onEvent(ItemFormEvent.OnSaveClicked) },
                        enabled = uiState.isTitleValid,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.item_form_save))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ItemFormV4Body(state = uiState, onEvent = onEvent, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun ItemFormV4Body(
    state: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        TypeSelectorField(type = state.type, onEvent = onEvent)

        FormField(label = stringResource(R.string.item_form_title_label), modifier = Modifier.padding(top = 16.dp)) {
            AppTextField(
                value = state.title,
                onValueChange = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
                placeholder = stringResource(R.string.item_form_title_label),
            )
        }

        FormField(
            label = stringResource(R.string.item_form_description_label),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            AppTextField(
                value = state.description,
                onValueChange = { onEvent(ItemFormEvent.OnDescriptionChanged(it)) },
                placeholder = stringResource(R.string.item_form_description_label),
                singleLine = false,
                minHeight = 96.dp,
            )
        }

        if (state.availableSections.isNotEmpty()) {
            SectionField(
                availableSections = state.availableSections,
                sectionId = state.sectionId,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (state.availableTags.isNotEmpty()) {
            TagsField(
                availableTags = state.availableTags,
                selectedTagIds = state.selectedTagIds,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        DueDateField(dueDate = state.dueDate, onEvent = onEvent, modifier = Modifier.padding(top = 16.dp))

        if (state.canPickRecurrence) {
            RecurrenceField(
                recurrence = state.recurrence,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormScreenV4Preview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        ItemFormV4Content(
            uiState = previewState.uiState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
