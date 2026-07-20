package com.seucaio.unideas.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.ds.components.legacy.AppVersionFooter
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.GroupHeader
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.settings.viewmodel.SettingsDialogState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsEvent
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiAction
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import com.seucaio.unideas.core.backup.R as BackupR

/**
 * V2 (#84): same [SettingsViewModel]/[SettingsUiState]/[SettingsEvent]/[SettingsDialogState] +
 * `BackupViewModel` contract as [SettingsScreen] — visual pass only. Rows swap from legacy
 * `UnideasListItem` to `:uds`'s native [NavRow] (icon + label + chevron); [NavRow] gained an
 * optional `subtitle` for this screen's Backup row, which needs real connection-status text, not
 * a mock. `GroupHeader` replaces the ad hoc section title `Text`. `SeedScopeBottomSheet`,
 * `UnideasTopBar`, `AppVersionFooter` and the dev-only `UseV2ScreensRow` stay legacy/as-is — no
 * native equivalent yet, or (for the toggle row) out of the redesign's scope entirely.
 */
@Composable
fun SettingsScreenV2(
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

    SettingsContentV2(
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

@Composable
private fun SettingsContentV2(
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
                SettingsBodyV2(
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
private fun SettingsBodyV2(
    onEvent: (SettingsEvent) -> Unit,
    backupUiState: BackupUiState,
    onBackupClick: () -> Unit,
    showDebugSection: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        GroupHeader(stringResource(R.string.settings_organize_section))
        NavRow(
            icon = Icons.Outlined.Folder,
            label = stringResource(R.string.settings_organize_sections),
            onClick = { onEvent(SettingsEvent.OnOrganizeSectionsClicked) },
        )
        NavRow(
            icon = Icons.AutoMirrored.Outlined.Label,
            label = stringResource(R.string.settings_organize_tags),
            onClick = { onEvent(SettingsEvent.OnOrganizeTagsClicked) },
        )

        GroupHeader(stringResource(R.string.settings_backup_section))
        NavRow(
            icon = Icons.Outlined.CloudUpload,
            label = stringResource(R.string.settings_backup_section),
            subtitle = backupStatusSubtitle(backupUiState),
            onClick = onBackupClick,
        )

        if (showDebugSection) {
            GroupHeader(stringResource(R.string.settings_debug_section))
            NavRow(
                icon = Icons.AutoMirrored.Outlined.List,
                label = stringResource(R.string.settings_debug_items),
                onClick = { onEvent(SettingsEvent.OnItemsClicked) },
            )
            NavRow(
                icon = Icons.Outlined.Storage,
                label = stringResource(R.string.settings_debug_seed),
                onClick = { onEvent(SettingsEvent.OnSeedDatabaseClicked) },
            )
            NavRow(
                icon = Icons.Outlined.DeleteSweep,
                label = stringResource(R.string.settings_debug_clear),
                onClick = { onEvent(SettingsEvent.OnClearDatabaseClicked) },
            )
            UseV2ScreensRowV2()
        }
    }
}

@Composable
private fun backupStatusSubtitle(backupUiState: BackupUiState): String = when (backupUiState) {
    is BackupUiState.Loading -> stringResource(BackupR.string.backup_not_connected)
    is BackupUiState.Ready -> if (backupUiState.isConnected) {
        backupUiState.lastBackupAt
            ?.let { stringResource(BackupR.string.backup_last_at, it.format(LAST_BACKUP_FORMATTER_V2)) }
            ?: stringResource(BackupR.string.backup_none)
    } else {
        stringResource(BackupR.string.backup_not_connected)
    }
}

private val LAST_BACKUP_FORMATTER_V2: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")

/** Same dev-only toggle as [SettingsScreen]'s `UseV2ScreensRow` — out of the redesign's scope. */
@Composable
private fun UseV2ScreensRowV2() {
    val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.settings_debug_use_v2_screens))
        Switch(checked = useV2, onCheckedChange = DevScreenVersionToggle::set)
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenV2Preview(
    @PreviewParameter(SettingsPreviewProvider::class) uiState: SettingsUiState,
) {
    UdsTheme {
        SettingsContentV2(
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
