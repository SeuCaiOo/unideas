package com.seucaio.unideas.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.ui.components.AppVersionFooter
import com.seucaio.unideas.core.ui.components.UnideasListItem
import com.seucaio.unideas.core.ui.components.UnideasTopBar
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.feature.settings.viewmodel.SettingsEvent
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiAction
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    versionName: String,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToItems: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updatedOnNavigateToSections by rememberUpdatedState(onNavigateToSections)
    val updatedOnNavigateToTags by rememberUpdatedState(onNavigateToTags)
    val updatedOnNavigateToItems by rememberUpdatedState(onNavigateToItems)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is SettingsUiAction.NavigateToSections -> updatedOnNavigateToSections()
                is SettingsUiAction.NavigateToTags -> updatedOnNavigateToTags()
                is SettingsUiAction.NavigateToItems -> updatedOnNavigateToItems()
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        versionName = versionName,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    versionName: String,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
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
        }
    ) { padding ->
        when (uiState) {
            is SettingsUiState.Success ->
                SettingsBody(onEvent = onEvent, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun SettingsBody(
    onEvent: (SettingsEvent) -> Unit,
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
            subtitle = stringResource(R.string.settings_backup_disconnected),
        )

        // Temporary entry point until Home (#27) ships as the real way to reach Items.
        SettingsSectionHeader(stringResource(R.string.settings_debug_section))
        UnideasListItem(
            title = stringResource(R.string.settings_debug_items),
            onClick = { onEvent(SettingsEvent.OnItemsClicked) },
        )
    }
}

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
            versionName = "0.0.2",
            onEvent = {},
            onNavigateBack = null,
        )
    }
}
