package com.seucaio.unideas.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.backup.BackupBottomSheet
import com.seucaio.unideas.core.backup.LogoutConfirmBottomSheet
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import com.seucaio.unideas.core.common.extensions.toFormattedDateTimeString
import com.seucaio.unideas.core.notifications.notification.ReminderNotifier
import com.seucaio.unideas.core.notifications.worker.ReminderScheduler
import com.seucaio.unideas.ds.components.legacy.AppVersionFooter
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.components.lists.ListSection
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.gallery.ComponentGallery
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.settings.viewmodel.SettingsAccountUiState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsDialogState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsEvent
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiAction
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.seucaio.unideas.core.backup.R as BackupR

@Composable
fun SettingsScreen(
    versionName: String,
    showDebugSection: Boolean,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToItems: () -> Unit,
    onLogoutComplete: () -> Unit,
    openBackupSheet: Boolean = false,
    viewModel: SettingsViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val backupUiState by backupViewModel.uiState.collectAsStateWithLifecycle()
    val accountUiState by viewModel.accountUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val updatedOnNavigateToSections by rememberUpdatedState(onNavigateToSections)
    val updatedOnNavigateToTags by rememberUpdatedState(onNavigateToTags)
    val updatedOnNavigateToItems by rememberUpdatedState(onNavigateToItems)
    val updatedOnLogoutComplete by rememberUpdatedState(onLogoutComplete)
    var showBackupSheet by remember { mutableStateOf(openBackupSheet) }
    var showDesignSystemGallery by remember { mutableStateOf(false) }
    var showTestNotificationSheet by remember { mutableStateOf(false) }
    var showLogoutSheet by remember { mutableStateOf(false) }

    val isBackupConnected = (backupUiState as? BackupUiState.Ready)?.isConnected == true
    LaunchedEffect(isBackupConnected) {
        if (isBackupConnected) viewModel.refreshAccountState()
    }

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

                SettingsUiAction.LogoutCompleted -> updatedOnLogoutComplete()
            }
        }
    }

    if (showDesignSystemGallery) {
        Scaffold(
            topBar = {
                UnideasTopBar(
                    title = stringResource(R.string.settings_debug_design_system),
                    onNavigateBack = { showDesignSystemGallery = false },
                )
            },
        ) { padding ->
            ComponentGallery(modifier = Modifier.padding(padding))
        }
        return
    }

    SettingsContent(
        uiState = uiState,
        dialogState = dialogState,
        backupUiState = backupUiState,
        accountUiState = accountUiState,
        versionName = versionName,
        showDebugSection = showDebugSection,
        onEvent = viewModel::onEvent,
        onBackupClick = { showBackupSheet = true },
        onLogoutClick = { showLogoutSheet = true },
        onDesignSystemGalleryClick = { showDesignSystemGallery = true },
        onRunReminderCheckClicked = {
            ReminderScheduler.refreshNow(context, silent = false)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(resources.getString(R.string.settings_debug_run_reminder_check_success))
            }
        },
        onTestNotificationClicked = { showTestNotificationSheet = true },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )

    BackupBottomSheet(
        visible = showBackupSheet,
        snackbarHostState = snackbarHostState,
        onDismiss = { showBackupSheet = false },
        viewModel = backupViewModel,
    )

    if (showLogoutSheet) {
        val accountEmail = accountUiState.accountEmail
        if (accountEmail != null) {
            LogoutConfirmBottomSheet(
                accountEmail = accountEmail,
                onDismiss = { showLogoutSheet = false },
                onConfirm = {
                    showLogoutSheet = false
                    viewModel.onEvent(SettingsEvent.OnLogoutConfirmed)
                },
            )
        }
    }

    if (showTestNotificationSheet) {
        TestNotificationBottomSheet(
            onSend = { urgent ->
                ReminderNotifier(context).notifyTest(urgent)
                showTestNotificationSheet = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        resources.getString(R.string.settings_debug_test_notification_success)
                    )
                }
            },
            onDismiss = { showTestNotificationSheet = false },
        )
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    dialogState: SettingsDialogState,
    backupUiState: BackupUiState,
    accountUiState: SettingsAccountUiState,
    versionName: String,
    showDebugSection: Boolean,
    onEvent: (SettingsEvent) -> Unit,
    onBackupClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDesignSystemGalleryClick: () -> Unit,
    onRunReminderCheckClicked: () -> Unit,
    onTestNotificationClicked: () -> Unit,
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
                    accountUiState = accountUiState,
                    onBackupClick = onBackupClick,
                    onLogoutClick = onLogoutClick,
                    onDesignSystemGalleryClick = onDesignSystemGalleryClick,
                    onRunReminderCheckClicked = onRunReminderCheckClicked,
                    onTestNotificationClicked = onTestNotificationClicked,
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
    accountUiState: SettingsAccountUiState,
    onBackupClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDesignSystemGalleryClick: () -> Unit,
    onRunReminderCheckClicked: () -> Unit,
    onTestNotificationClicked: () -> Unit,
    showDebugSection: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (accountUiState.isConnected) {
            AccountCard(
                accountName = accountUiState.accountName,
                accountEmail = accountUiState.accountEmail,
                onLogoutClick = onLogoutClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        ListSection(title = stringResource(R.string.settings_organize_section)) {
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
        }

        ListSection(title = stringResource(R.string.settings_backup_section)) {
            NavRow(
                icon = Icons.Outlined.CloudUpload,
                label = stringResource(R.string.settings_backup_section),
                subtitle = backupStatusSubtitle(backupUiState),
                onClick = onBackupClick,
            )
        }

        if (showDebugSection) {
            ListSection(title = stringResource(R.string.settings_debug_section)) {
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
                NavRow(
                    icon = Icons.Outlined.Palette,
                    label = stringResource(R.string.settings_debug_design_system),
                    onClick = onDesignSystemGalleryClick,
                )
                NavRow(
                    icon = Icons.Outlined.Sync,
                    label = stringResource(R.string.settings_debug_run_reminder_check),
                    onClick = onRunReminderCheckClicked,
                )
                NavRow(
                    icon = Icons.Outlined.Notifications,
                    label = stringResource(R.string.settings_debug_test_notification),
                    onClick = onTestNotificationClicked,
                )
                ScreenVersionRow()
            }
        }
    }
}

@Composable
private fun backupStatusSubtitle(backupUiState: BackupUiState): String = when (backupUiState) {
    is BackupUiState.Loading -> stringResource(BackupR.string.backup_not_connected)
    is BackupUiState.Ready -> if (backupUiState.isConnected) {
        val lastBackup = backupUiState.lastBackupAt
            ?.let { stringResource(BackupR.string.backup_last_at, it.toFormattedDateTimeString()) }
            ?: stringResource(BackupR.string.backup_none)
        if (backupUiState.isAutoBackupEnabled) {
            "$lastBackup · ${stringResource(BackupR.string.backup_auto_backup_enabled_tag)}"
        } else {
            lastBackup
        }
    } else {
        stringResource(BackupR.string.backup_not_connected)
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsPreviewProvider::class) scenario: SettingsScreenPreviewScenario,
) {
    UdsTheme {
        SettingsContent(
            uiState = scenario.uiState,
            dialogState = SettingsDialogState.None,
            backupUiState = BackupUiState.Ready(isConnected = false),
            accountUiState = scenario.accountUiState,
            versionName = "0.0.2",
            showDebugSection = true,
            onEvent = {},
            onBackupClick = {},
            onLogoutClick = {},
            onDesignSystemGalleryClick = {},
            onRunReminderCheckClicked = {},
            onTestNotificationClicked = {},
            onNavigateBack = null,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
