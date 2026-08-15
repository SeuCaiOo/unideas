package com.seucaio.unideas.feature.items.ui.screens.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.SwitchSection
import com.seucaio.unideas.ds.components.legacy.DeleteConfirmationDialog
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.DueDateField
import com.seucaio.unideas.feature.items.ui.components.fields.DueTimeField
import com.seucaio.unideas.feature.items.ui.components.fields.ReminderWarningField
import com.seucaio.unideas.feature.items.ui.components.fields.SectionField
import com.seucaio.unideas.feature.items.ui.components.fields.TagsField
import com.seucaio.unideas.feature.items.ui.components.fields.recurrence.RecurrenceField
import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.ItemConfigDialogState
import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.ItemConfigEvent
import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.ItemConfigUiAction
import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.ItemConfigUiState
import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.ItemConfigViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ItemConfigScreen(
    itemId: Long,
    onNavigateBack: (() -> Unit)?,
    viewModel: ItemConfigViewModel = koinViewModel { parametersOf(itemId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemConfigUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemConfigUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ItemConfigScreenContent(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemConfigScreenContent(
    uiState: ItemConfigUiState,
    dialogState: ItemConfigDialogState,
    onEvent: (ItemConfigEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.item_config_title),
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.isLoading -> UnideasLoadingContent(modifier = Modifier.padding(padding))
            uiState.loadFailed -> UnideasErrorContent(
                messageRes = R.string.item_form_load_error,
                onRetry = { onEvent(ItemConfigEvent.OnRetryClicked) },
                modifier = Modifier.padding(padding),
            )
            else -> ItemConfigFields(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (dialogState is ItemConfigDialogState.TypeSwitchConfirm) {
        DeleteConfirmationDialog(
            titleRes = R.string.item_config_type_switch_title,
            messageRes = R.string.item_config_type_switch_message,
            onDismiss = { onEvent(ItemConfigEvent.OnDialogDismissed) },
            onConfirm = { onEvent(ItemConfigEvent.OnTypeSwitchConfirmClicked) },
        )
    }
}

@Composable
private fun ItemConfigFields(
    uiState: ItemConfigUiState,
    onEvent: (ItemConfigEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        SectionLabel(stringResource(R.string.item_config_section_organization), Modifier.padding(top = 16.dp))
        OrganizationFields(uiState, onEvent)

        SectionLabel(stringResource(R.string.item_config_section_recurrence), Modifier.padding(top = 24.dp))
        RecurrenceAndReminderFields(uiState, onEvent, Modifier.padding(top = 8.dp))

        SectionLabel(stringResource(R.string.item_config_section_danger_zone), Modifier.padding(top = 24.dp))
        TypeDangerZone(
            type = uiState.type,
            onChangeTypeClicked = {
                val newType = if (uiState.type == ItemType.TASK) ItemType.NOTE else ItemType.TASK
                onEvent(ItemConfigEvent.OnChangeTypeClicked(newType))
            },
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun OrganizationFields(uiState: ItemConfigUiState, onEvent: (ItemConfigEvent) -> Unit) {
    Column {
        if (uiState.availableSections.isNotEmpty()) {
            SectionField(
                availableSections = uiState.availableSections,
                sectionId = uiState.sectionId,
                onSectionChanged = { onEvent(ItemConfigEvent.OnSectionChanged(it)) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (uiState.availableTags.isNotEmpty()) {
            TagsField(
                availableTags = uiState.availableTags,
                selectedTagIds = uiState.selectedTagIds,
                onTagToggled = { onEvent(ItemConfigEvent.OnTagToggled(it)) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun RecurrenceAndReminderFields(
    uiState: ItemConfigUiState,
    onEvent: (ItemConfigEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwitchSection(
        label = stringResource(R.string.item_form_has_reminder_label),
        checked = uiState.hasReminder,
        onCheckedChange = { onEvent(ItemConfigEvent.OnReminderToggled(it)) },
        modifier = modifier,
    ) {
        RecurrenceField(
            recurrence = uiState.recurrence,
            dueDate = uiState.dueDate,
            onRecurrenceChanged = { onEvent(ItemConfigEvent.OnRecurrenceChanged(it)) },
            onDueDateChanged = { onEvent(ItemConfigEvent.OnDueDateChanged(it)) },
            modifier = Modifier.padding(top = 16.dp),
        )

        if (uiState.recurrence == Recurrence.None) {
            DueDateField(
                dueDate = uiState.dueDate,
                onDueDateChanged = { onEvent(ItemConfigEvent.OnDueDateChanged(it)) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        DueTimeField(
            dueTime = uiState.dueTime,
            onDueTimeChanged = { onEvent(ItemConfigEvent.OnDueTimeChanged(it)) },
            modifier = Modifier.padding(top = 16.dp),
        )

        ReminderWarningField(
            reminderWarning = uiState.reminderWarning,
            onReminderWarningChanged = { onEvent(ItemConfigEvent.OnReminderWarningChanged(it)) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, modifier = modifier)
}

@Composable
private fun TypeDangerZone(type: ItemType, onChangeTypeClicked: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = if (type == ItemType.TASK) {
                        stringResource(R.string.item_form_type_task)
                    } else {
                        stringResource(R.string.item_form_type_note)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = if (type == ItemType.TASK) {
                        stringResource(R.string.item_config_type_subtitle_task)
                    } else {
                        stringResource(R.string.item_config_type_subtitle_note)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onChangeTypeClicked) {
                Text(stringResource(R.string.item_config_type_change_button))
            }
        }
    }
}

private class ItemConfigPreviewProvider : PreviewParameterProvider<ItemConfigUiState> {
    override val values: Sequence<ItemConfigUiState> = sequenceOf(
        ItemConfigUiState(isLoading = false, type = ItemType.TASK, hasReminder = true),
        ItemConfigUiState(isLoading = false, type = ItemType.NOTE),
    )
}

@PreviewLightDark
@Composable
private fun ItemConfigScreenPreview(
    @PreviewParameter(ItemConfigPreviewProvider::class) previewState: ItemConfigUiState,
) {
    UdsTheme {
        ItemConfigScreenContent(
            uiState = previewState,
            dialogState = ItemConfigDialogState.None,
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
