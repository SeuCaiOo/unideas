package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R

/**
 * V2 (#84): real Material3 `SecondaryTabRow` + `Tab` — not a hand-rolled `Row` of `:uds`'s bare
 * `TabItem` (that component has none of `TabRow`'s indicator animation, or tab
 * semantics/accessibility). Styled with `:uds` tokens ([AppType.TabLabel]) to keep the 1a Tonal
 * look while getting Material3's tab mechanics for free. Shared between [HomeScreenV2] and
 * `BrowseScreen` — both switch the same [ItemType] tab over the same
 * [com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel] state.
 */
@Composable
fun TasksNotesTabRowV2(activeTab: ItemType, onTabSelect: (ItemType) -> Unit, modifier: Modifier = Modifier) {
    SecondaryTabRow(selectedTabIndex = activeTab.tabIndex(), modifier = modifier) {
        Tab(
            selected = activeTab == ItemType.TASK,
            onClick = { onTabSelect(ItemType.TASK) },
            text = { Text(stringResource(R.string.home_tab_tasks), style = AppType.TabLabel) },
        )
        Tab(
            selected = activeTab == ItemType.NOTE,
            onClick = { onTabSelect(ItemType.NOTE) },
            text = { Text(stringResource(R.string.home_tab_notes), style = AppType.TabLabel) },
        )
    }
}

private fun ItemType.tabIndex(): Int = when (this) {
    ItemType.TASK -> 0
    ItemType.NOTE -> 1
}

@PreviewLightDark
@Composable
private fun TasksNotesTabRowV2Preview() {
    UdsTheme {
        Surface {
            TasksNotesTabRowV2(activeTab = ItemType.TASK, onTabSelect = {})
        }
    }
}
