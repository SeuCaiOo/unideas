package com.seucaio.unideas.feature.home.features.home.screen.components.chrome

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode
import java.time.LocalDateTime

@Composable
internal fun HomeTopBar(
    homeMode: HomeMode,
    itemsState: HomeItemsState,
    hasAnyPriorityItem: Boolean,
    isAutoBackupEnabled: Boolean,
    onNavigateBack: (() -> Unit)?,
    onShowPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowBackup: () -> Unit,
    onEvent: (HomeEvent) -> Unit,
) {
    if (homeMode is HomeMode.Selection) {
        val allSelected = itemsState.tabItems.isNotEmpty() &&
            itemsState.tabItems.map { it.id }.toSet() == homeMode.selectedItemIds
        UnideasTopBar(
            title = pluralStringResource(
                R.plurals.home_selection_count,
                homeMode.selectedItemIds.size,
                homeMode.selectedItemIds.size,
            ),
            onNavigateBack = { onEvent(HomeEvent.OnSelectionCleared) },
            navigationBackIcon = Icons.Default.Close,
            actions = {
                IconButton(onClick = { onEvent(HomeEvent.OnSelectAllClicked) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.PlaylistAddCheck,
                        contentDescription = stringResource(
                            if (allSelected) {
                                R.string.home_selection_clear
                            } else {
                                R.string.home_selection_select_all
                            },
                        ),
                    )
                }
            },
        )
    } else {
        UnideasTopBar(
            title = stringResource(R.string.home_title),
            onNavigateBack = onNavigateBack,
            actions = {
                HomeTopBarActions(
                    hasAnyPriorityItem = hasAnyPriorityItem,
                    isAutoBackupEnabled = isAutoBackupEnabled,
                    onShowPriorities = onShowPriorities,
                    onNavigateToSettings = onNavigateToSettings,
                    onShowBackup = onShowBackup,
                )
            },
        )
    }
}

@Composable
private fun RowScope.HomeTopBarActions(
    hasAnyPriorityItem: Boolean,
    isAutoBackupEnabled: Boolean,
    onShowPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowBackup: () -> Unit,
) {
    IconButton(onClick = onShowBackup) {
        Icon(
            imageVector = if (isAutoBackupEnabled) Icons.Outlined.CloudSync else Icons.Outlined.CloudOff,
            contentDescription = stringResource(
                if (isAutoBackupEnabled) R.string.home_backup_action_enabled else R.string.home_backup_action_disabled
            ),
        )
    }
    if (hasAnyPriorityItem) {
        IconButton(onClick = onShowPriorities) {
            Icon(
                Icons.Outlined.Flag,
                contentDescription = stringResource(R.string.priority_panel_title),
            )
        }
    }
    IconButton(onClick = onNavigateToSettings) {
        Icon(
            Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.home_settings_action),
        )
    }
}

private val homeTopBarPreviewItems = listOf(
    Item(id = 1L, type = ItemType.TASK, title = "Pay electricity bill", createdAt = LocalDateTime.now()),
    Item(id = 2L, type = ItemType.TASK, title = "Buy groceries", createdAt = LocalDateTime.now()),
)

private class HomeTopBarPreviewProvider : PreviewParameterProvider<HomeMode> {
    override val values = sequenceOf(
        HomeMode.Normal,
        HomeMode.Selection(homeTopBarPreviewItems.map { it.id }.toSet()),
    )
}

@PreviewLightDark
@Composable
private fun HomeTopBarPreview(@PreviewParameter(HomeTopBarPreviewProvider::class) homeMode: HomeMode) {
    UdsTheme {
        Surface {
            HomeTopBar(
                homeMode = homeMode,
                itemsState = HomeItemsState(tabItems = homeTopBarPreviewItems),
                hasAnyPriorityItem = true,
                isAutoBackupEnabled = true,
                onNavigateBack = {},
                onShowPriorities = {},
                onNavigateToSettings = {},
                onShowBackup = {},
                onEvent = {},
            )
        }
    }
}
