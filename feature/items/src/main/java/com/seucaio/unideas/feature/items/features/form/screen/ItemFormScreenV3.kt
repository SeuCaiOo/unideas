package com.seucaio.unideas.feature.items.features.form.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.chips.TextBadge
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.MetaChipsRow
import com.seucaio.unideas.ds.components.lists.MetaRow
import com.seucaio.unideas.ds.components.lists.TitleSubtitle
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.screen.components.ItemFormBody
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiAction
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiState
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * #86 Pacote 2, direção V3: **uma única tela**, sem navegar, alternando entre o layout de leitura
 * de hoje ([com.seucaio.unideas.feature.items.features.detail.screen.ItemDetailScreen] —
 * [TitleSubtitle]/[MetaRow]/[MetaChipsRow]/[TextBadge]) e o layout de edição, que reaproveita o
 * [ItemFormBody] compartilhado (só título/descrição têm estilo próprio aqui). Ao contrário de [ItemFormScreenV2]
 * (que unifica visualmente os dois estados sob o mesmo layout de formulário), V3 mantém os dois
 * visuais existentes intocados — o que muda é que agora vivem na mesma tela/rota, sem duas
 * navegações separadas. Reaproveita [ItemFormViewModel]/[ItemFormUiState]/[ItemFormEvent] como
 * estão. Sem rota nova no nav graph ainda (decisão de navegação adiada, #86) — visível só via
 * `@PreviewLightDark`.
 */
@Composable
fun ItemFormScreenV3(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    var isEditingFields by remember(itemId) { mutableStateOf(itemId == null) }

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

    ItemFormV3Content(
        isEditingFields = isEditingFields,
        onToggleEditingFields = { isEditingFields = !isEditingFields },
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormV3Content(
    isEditingFields: Boolean,
    onToggleEditingFields: () -> Unit,
    uiState: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val title =
        stringResource(if (isEditingFields) R.string.item_form_title_edit else R.string.item_form_title_view) + " — V3"

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = title,
                onNavigateBack = updatedOnNavigateBack,
                actions = {
                    if (isEditingFields) {
                        IconButton(
                            onClick = {
                                onEvent(ItemFormEvent.OnSaveClicked)
                                onToggleEditingFields()
                            },
                            enabled = uiState.isTitleValid,
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.item_form_save))
                        }
                    } else {
                        IconButton(onClick = onToggleEditingFields) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.item_form_v2_edit))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (isEditingFields) {
            ItemFormBody(
                state = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
                titleDescriptionFields = { state, fieldEvent -> ItemFormV3TitleDescriptionFields(state, fieldEvent) },
            )
        } else {
            ItemFormV3ViewBody(state = uiState, modifier = Modifier.padding(padding))
        }
    }
}

/** V3-only title/description rendering — filled [TextField] (Material3 default), not outlined like [ItemFormBody]'s default. */
@Composable
private fun ItemFormV3TitleDescriptionFields(state: ItemFormUiState, onEvent: (ItemFormEvent) -> Unit) {
    ItemFormV3TextField(
        value = state.title,
        onValueChange = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
        placeholder = stringResource(R.string.item_form_title_label),
        textStyle = MaterialTheme.typography.headlineLarge,
    )

    ItemFormV3TextField(
        value = state.description,
        onValueChange = { onEvent(ItemFormEvent.OnDescriptionChanged(it)) },
        placeholder = stringResource(R.string.item_form_description_label),
        singleLine = false,
        minHeight = 96.dp,
        textStyle = MaterialTheme.typography.titleLarge,
    )
}

/** V3-only text field style: filled (Material3 default), not outlined like [com.seucaio.unideas.ds.components.inputs.AppTextField]. */
@Composable
private fun ItemFormV3TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 0.dp,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    var sized = modifier.fillMaxWidth()
    if (minHeight > 0.dp) sized = sized.defaultMinSize(minHeight = minHeight)

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = textStyle) },
        singleLine = singleLine,
        textStyle = textStyle,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        modifier = sized,
    )
}

@Composable
private fun ItemFormV3ViewBody(state: ItemFormUiState, modifier: Modifier = Modifier) {
    val sectionName = state.availableSections.firstOrNull { it.id == state.sectionId }?.name
    val typeRes = if (state.type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TitleSubtitle(
            title = state.title.ifBlank { stringResource(R.string.item_form_title_label) },
            subtitle = state.description.ifBlank { null },
            modifier = Modifier.padding(16.dp),
        )

        TextBadge(
            text = stringResource(typeRes),
            background = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        )

        MetaRow(
            label = stringResource(R.string.item_detail_section_label),
            value = sectionName ?: stringResource(R.string.item_form_section_none),
        )

        if (state.selectedTagIds.isNotEmpty()) {
            val tagNames = state.availableTags.filter { it.id in state.selectedTagIds }.map { it.name }
            MetaChipsRow(label = stringResource(R.string.item_detail_tags_label), chips = tagNames)
        }

        MetaRow(
            label = stringResource(R.string.item_form_date_label),
            value = state.dueDate?.toFormattedDateString() ?: stringResource(R.string.item_form_section_none),
            isLast = state.recurrence == Recurrence.None || state.dueDate == null,
        )

        if (state.dueDate != null && state.recurrence != Recurrence.None) {
            MetaRow(
                label = stringResource(R.string.item_detail_recurrence_label),
                value = v3RecurrenceLabel(state.recurrence),
                isLast = true,
            )
        }
    }
}

@Composable
private fun v3RecurrenceLabel(recurrence: Recurrence): String = when (recurrence) {
    Recurrence.Daily -> stringResource(R.string.item_form_recurrence_daily)
    Recurrence.Weekly -> stringResource(R.string.item_form_recurrence_weekly)
    Recurrence.Monthly -> stringResource(R.string.item_form_recurrence_monthly)
    else -> stringResource(R.string.item_form_recurrence_none)
}

@PreviewLightDark
@Composable
private fun ItemFormScreenV3Preview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        var isEditingFields by remember(previewState) { mutableStateOf(!previewState.isEditing) }
        ItemFormV3Content(
            isEditingFields = isEditingFields,
            onToggleEditingFields = { isEditingFields = !isEditingFields },
            uiState = previewState.uiState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
