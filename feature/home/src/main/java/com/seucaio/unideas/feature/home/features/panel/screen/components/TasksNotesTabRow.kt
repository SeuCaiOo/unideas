package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.navigation.AppTabRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R

/**
 * Thin [ItemType]<->index bridge over `:uds`'s domain-agnostic `AppTabRow` (real Material3
 * `SecondaryTabRow`/`Tab` under the hood, styled with `:uds` tokens). Shared between
 * [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen] and
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen` — both switch the same
 * [ItemType] tab over the same
 * [com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel] state.
 */
@Composable
fun TasksNotesTabRow(activeTab: ItemType, onTabSelect: (ItemType) -> Unit, modifier: Modifier = Modifier) {
    AppTabRow(
        tabs = listOf(stringResource(R.string.home_tab_tasks), stringResource(R.string.home_tab_notes)),
        selectedIndex = activeTab.tabIndex(),
        onTabSelected = { index -> onTabSelect(index.toItemType()) },
        modifier = modifier,
    )
}

private fun ItemType.tabIndex(): Int = when (this) {
    ItemType.TASK -> 0
    ItemType.NOTE -> 1
}

private fun Int.toItemType(): ItemType = when (this) {
    0 -> ItemType.TASK
    else -> ItemType.NOTE
}

@PreviewLightDark
@Composable
private fun TasksNotesTabRowPreview() {
    UdsTheme {
        Surface {
            TasksNotesTabRow(activeTab = ItemType.TASK, onTabSelect = {})
        }
    }
}
