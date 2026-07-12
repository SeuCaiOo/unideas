package com.seucaio.unideas.feature.items.features.form.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.extensions.toEpochMilliUtc
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.core.common.extensions.toLocalDateUtc
import com.seucaio.unideas.core.ui.components.SectionDropdown
import com.seucaio.unideas.core.ui.components.TagChip
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiAction
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiState
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ItemFormScreen(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId) },
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

    ItemFormContent(
        isEditing = itemId != null,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormContent(
    isEditing: Boolean,
    uiState: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val title = stringResource(if (isEditing) R.string.item_form_title_edit else R.string.item_form_title_create)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = title,
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
        ItemFormBody(state = uiState, onEvent = onEvent, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun ItemFormBody(
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
        TypeSelector(type = state.type, onEvent = onEvent)

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(ItemFormEvent.OnTitleChanged(it)) },
            label = { Text(stringResource(R.string.item_form_title_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(ItemFormEvent.OnDescriptionChanged(it)) },
            label = { Text(stringResource(R.string.item_form_description_label)) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )

        if (state.availableSections.isNotEmpty()) {
            SectionDropdown(
                options = state.availableSections.map { it.id to it.name },
                selectedId = state.sectionId,
                onSelect = { onEvent(ItemFormEvent.OnSectionChanged(it)) },
                noFilterLabel = stringResource(R.string.item_form_section_none),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
        }

        if (state.availableTags.isNotEmpty()) {
            TagsSection(
                availableTags = state.availableTags,
                selectedTagIds = state.selectedTagIds,
                onEvent = onEvent,
            )
        }

        DateAndRecurrenceSection(state = state, onEvent = onEvent)
    }
}

@Composable
private fun TypeSelector(type: ItemType, onEvent: (ItemFormEvent) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = type == ItemType.TASK,
            onClick = { onEvent(ItemFormEvent.OnTypeChanged(ItemType.TASK)) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(stringResource(R.string.item_form_type_task))
        }
        SegmentedButton(
            selected = type == ItemType.NOTE,
            onClick = { onEvent(ItemFormEvent.OnTypeChanged(ItemType.NOTE)) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text(stringResource(R.string.item_form_type_note))
        }
    }
}

@Composable
private fun TagsSection(
    availableTags: List<Tag>,
    selectedTagIds: Set<Long>,
    onEvent: (ItemFormEvent) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.item_form_tags_label),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        LazyRow {
            items(availableTags, key = { it.id }) { tag ->
                TagChip(
                    label = tag.name,
                    selected = tag.id in selectedTagIds,
                    onClick = { onEvent(ItemFormEvent.OnTagToggled(tag.id)) },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateAndRecurrenceSection(state: ItemFormUiState, onEvent: (ItemFormEvent) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(state.dueDate?.toFormattedDateString() ?: stringResource(R.string.item_form_date_none))
            }
            if (state.dueDate != null) {
                IconButton(onClick = { onEvent(ItemFormEvent.OnDueDateChanged(null)) }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.item_form_date_clear))
                }
            }
        }

        if (state.canPickRecurrence) {
            RecurrenceDropdown(
                recurrence = state.recurrence,
                onSelect = { onEvent(ItemFormEvent.OnRecurrenceChanged(it)) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dueDate?.toEpochMilliUtc())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newDate = datePickerState.selectedDateMillis?.toLocalDateUtc()
                    newDate?.let { onEvent(ItemFormEvent.OnDueDateChanged(it)) }
                    showDatePicker = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RecurrenceDropdown(
    recurrence: Recurrence,
    onSelect: (Recurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        Recurrence.None to stringResource(R.string.item_form_recurrence_none),
        Recurrence.Daily to stringResource(R.string.item_form_recurrence_daily),
        Recurrence.Weekly to stringResource(R.string.item_form_recurrence_weekly),
        Recurrence.Monthly to stringResource(R.string.item_form_recurrence_monthly),
    )

    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(options.first { it.first == recurrence }.second)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemFormScreenPreview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UnideasTheme {
        ItemFormContent(
            isEditing = previewState.isEditing,
            uiState = previewState.uiState,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
