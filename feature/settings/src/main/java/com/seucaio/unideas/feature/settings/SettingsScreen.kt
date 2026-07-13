package com.seucaio.unideas.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.seucaio.unideas.core.backup.BackupBottomSheet
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import com.seucaio.unideas.core.ui.components.AppVersionFooter
import com.seucaio.unideas.core.ui.components.UnideasListItem
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.feature.settings.viewmodel.SettingsDialogState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsEvent
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiAction
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import com.seucaio.unideas.core.backup.R as BackupR

@Composable
fun SettingsScreen(
    versionName: String,
    showDebugSection: Boolean,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToItems: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val backupUiState by backupViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val updatedOnNavigateToSections by rememberUpdatedState(onNavigateToSections)
    val updatedOnNavigateToTags by rememberUpdatedState(onNavigateToTags)
    val updatedOnNavigateToItems by rememberUpdatedState(onNavigateToItems)
    var showBackupSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is SettingsUiAction.NavigateToSections -> updatedOnNavigateToSections()
                is SettingsUiAction.NavigateToTags -> updatedOnNavigateToTags()
                is SettingsUiAction.NavigateToItems -> updatedOnNavigateToItems()
                is SettingsUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is SettingsUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes),
                )
                is SettingsUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        dialogState = dialogState,
        backupUiState = backupUiState,
        versionName = versionName,
        showDebugSection = showDebugSection,
        onEvent = viewModel::onEvent,
        onBackupClick = { showBackupSheet = true },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )

    if (showBackupSheet) {
        BackupBottomSheet(
            snackbarHostState = snackbarHostState,
            onDismiss = { showBackupSheet = false },
            viewModel = backupViewModel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    dialogState: SettingsDialogState,
    backupUiState: BackupUiState,
    versionName: String,
    showDebugSection: Boolean,
    onEvent: (SettingsEvent) -> Unit,
    onBackupClick: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(title = stringResource(R.string.settings_title), onNavigateBack = updatedOnNavigateBack)
        },
        bottomBar = {
            AppVersionFooter(
                versionName = versionName,
                modifier = Modifier.padding(16.dp),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (uiState) {
            is SettingsUiState.Success ->
                SettingsBody(
                    onEvent = onEvent,
                    backupUiState = backupUiState,
                    onBackupClick = onBackupClick,
                    showDebugSection = showDebugSection,
                    modifier = Modifier.padding(padding),
                )
        }
    }

    if (dialogState is SettingsDialogState.SelectingSeedScope) {
        SeedScopeBottomSheet(
            selectedScope = dialogState.selectedScope,
            onScopeSelect = { onEvent(SettingsEvent.OnSeedScopeSelected(it)) },
            onConfirm = { onEvent(SettingsEvent.OnSeedConfirmClicked) },
            onDismiss = { onEvent(SettingsEvent.OnSeedDialogDismissed) },
        )
    }
}

@Composable
private fun SettingsBody(
    onEvent: (SettingsEvent) -> Unit,
    backupUiState: BackupUiState,
    onBackupClick: () -> Unit,
    showDebugSection: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SettingsSectionHeader(stringResource(R.string.settings_organize_section))
        UnideasListItem(
            title = stringResource(R.string.settings_organize_sections),
            onClick = { onEvent(SettingsEvent.OnOrganizeSectionsClicked) },
        )
        UnideasListItem(
            title = stringResource(R.string.settings_organize_tags),
            onClick = { onEvent(SettingsEvent.OnOrganizeTagsClicked) },
        )

        SettingsSectionHeader(stringResource(R.string.settings_backup_section))
        UnideasListItem(
            title = stringResource(R.string.settings_backup_section),
            subtitle = backupStatusSubtitle(backupUiState),
            onClick = onBackupClick,
        )

        if (showDebugSection) {
            SettingsSectionHeader(stringResource(R.string.settings_debug_section))
            UnideasListItem(
                title = stringResource(R.string.settings_debug_items),
                onClick = { onEvent(SettingsEvent.OnItemsClicked) },
            )
            UnideasListItem(
                title = stringResource(R.string.settings_debug_seed),
                onClick = { onEvent(SettingsEvent.OnSeedDatabaseClicked) },
            )
            UnideasListItem(
                title = stringResource(R.string.settings_debug_clear),
                onClick = { onEvent(SettingsEvent.OnClearDatabaseClicked) },
            )
        }
    }
}

@Composable
private fun backupStatusSubtitle(backupUiState: BackupUiState): String = when (backupUiState) {
    is BackupUiState.Loading -> stringResource(BackupR.string.backup_not_connected)
    is BackupUiState.Ready -> if (backupUiState.isConnected) {
        backupUiState.lastBackupAt
            ?.let { stringResource(BackupR.string.backup_last_at, it.format(LAST_BACKUP_FORMATTER)) }
            ?: stringResource(BackupR.string.backup_none)
    } else {
        stringResource(BackupR.string.backup_not_connected)
    }
}

private val LAST_BACKUP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm")

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
    )
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsPreviewProvider::class) uiState: SettingsUiState,
) {
    UnideasTheme {
        SettingsContent(
            uiState = uiState,
            dialogState = SettingsDialogState.None,
            backupUiState = BackupUiState.Ready(isConnected = false),
            versionName = "0.0.2",
            showDebugSection = true,
            onEvent = {},
            onBackupClick = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
