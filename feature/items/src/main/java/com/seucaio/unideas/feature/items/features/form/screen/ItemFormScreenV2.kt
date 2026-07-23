package com.seucaio.unideas.feature.items.features.form.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
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
 * #86 Pacote 2, direção V2: instead of [ItemDetailScreen][com.seucaio.unideas.feature.items.features.detail.screen.ItemDetailScreen]
 * (read-only summary layout) and [ItemFormScreen] (form layout) looking like two unrelated
 * screens, this variant renders every field through the *same* [FormField]-wrapped layout in
 * both states — only the value area's interactivity changes (static text / read-only text field
 * vs an editable one). Reuses [ItemFormViewModel]/[ItemFormUiState]/[ItemFormEvent] as-is (no new
 * business logic, purely a visual variant) — navigation wiring is intentionally deferred until a
 * direction is chosen (#86), so this screen has no route yet; it's reached via
 * [ItemFormScreenV2Preview] for now. `readOnly` starts `true` when opened for an existing item
 * (mirrors today's Detail-first flow) and flips via the topbar pencil/check action — purely local
 * state, no persistence semantics tied to it beyond what [ItemFormEvent.OnSaveClicked] already
 * does.
 */
@Composable
fun ItemFormScreenV2(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    var readOnly by remember(itemId) { mutableStateOf(itemId != null) }

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

    ItemFormV2Content(
        readOnly = readOnly,
        onToggleReadOnly = { readOnly = !readOnly },
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormV2Content(
    readOnly: Boolean,
    onToggleReadOnly: () -> Unit,
    uiState: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val title =
        stringResource(if (readOnly) R.string.item_form_title_edit else R.string.item_form_title_create) + " — V2"

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = title,
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    if (readOnly) {
                        IconButton(onClick = onToggleReadOnly) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.item_form_v2_edit))
                        }
                    } else {
                        IconButton(
                            onClick = { onEvent(ItemFormEvent.OnSaveClicked) },
                            enabled = uiState.isTitleValid,
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.item_form_save))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ItemFormV2Body(
            state = uiState,
            readOnly = readOnly,
            onEvent = onEvent,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun ItemFormV2Body(
    state: ItemFormUiState,
    readOnly: Boolean,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        ReadOnlyOverlay(readOnly = readOnly) {
            TypeSelectorField(type = state.type, onEvent = onEvent)
        }

        FormField(label = stringResource(R.string.item_form_title_label), modifier = Modifier.padding(top = 16.dp)) {
            AppTextField(
                value = state.title,
                onValueChange = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
                placeholder = stringResource(R.string.item_form_title_label),
                readOnly = readOnly,
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
                readOnly = readOnly,
            )
        }

        if (state.availableSections.isNotEmpty()) {
            ReadOnlyOverlay(readOnly = readOnly, modifier = Modifier.padding(top = 16.dp)) {
                SectionField(
                    availableSections = state.availableSections,
                    sectionId = state.sectionId,
                    onEvent = onEvent
                )
            }
        }

        if (state.availableTags.isNotEmpty()) {
            ReadOnlyOverlay(readOnly = readOnly, modifier = Modifier.padding(top = 16.dp)) {
                TagsField(availableTags = state.availableTags, selectedTagIds = state.selectedTagIds, onEvent = onEvent)
            }
        }

        ReadOnlyOverlay(readOnly = readOnly, modifier = Modifier.padding(top = 16.dp)) {
            DueDateField(dueDate = state.dueDate, onEvent = onEvent)
        }

        if (state.canPickRecurrence) {
            ReadOnlyOverlay(readOnly = readOnly, modifier = Modifier.padding(top = 16.dp)) {
                RecurrenceField(recurrence = state.recurrence, onEvent = onEvent)
            }
        }
    }
}

/**
 * Field components under `screen/components/` (dropdowns, date picker, tag chips) don't expose a
 * `readOnly` toggle of their own — rather than thread one through every field just for this
 * exploratory variant, an invisible touch-absorbing layer on top blocks interaction while keeping
 * the exact same visual (slightly dimmed as the read-only cue) so V2 can stay a pure visual
 * experiment without reshaping those shared components ahead of a direction being chosen.
 */
@Composable
private fun ReadOnlyOverlay(readOnly: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        Box(modifier = if (readOnly) Modifier.alpha(0.6f) else Modifier) {
            content()
        }
        if (readOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) { detectTapGestures {} },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormScreenV2Preview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        var readOnly by remember(previewState) { mutableStateOf(previewState.isEditing) }
        ItemFormV2Content(
            readOnly = readOnly,
            onToggleReadOnly = { readOnly = !readOnly },
            uiState = previewState.uiState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
