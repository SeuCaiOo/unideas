package com.seucaio.unideas.feature.home.features.home.screen.components.chrome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.buttons.AppFab
import com.seucaio.unideas.ds.components.buttons.MiniFabAction
import com.seucaio.unideas.ds.components.legacy.ConditionalFab
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode

@Composable
internal fun HomeFab(
    visible: Boolean,
    homeMode: HomeMode,
    addMenuExpanded: Boolean,
    onAddMenuExpandedChange: (Boolean) -> Unit,
    onEvent: (HomeEvent) -> Unit,
) {
    ConditionalFab(visible = visible) {
        if (homeMode is HomeMode.Selection) {
            SelectionFab(
                expanded = addMenuExpanded,
                onToggle = { onAddMenuExpandedChange(!addMenuExpanded) },
                onDelete = {
                    onAddMenuExpandedChange(false)
                    onEvent(HomeEvent.OnDeleteSelectedClicked)
                },
                onArchive = {
                    onAddMenuExpandedChange(false)
                    onEvent(HomeEvent.OnArchiveSelectedClicked)
                },
            )
        } else {
            AddItemFab(
                expanded = addMenuExpanded,
                onToggle = { onAddMenuExpandedChange(!addMenuExpanded) },
                onAddTask = {
                    onAddMenuExpandedChange(false)
                    onEvent(HomeEvent.OnAddClicked(ItemType.TASK))
                },
                onAddNote = {
                    onAddMenuExpandedChange(false)
                    onEvent(HomeEvent.OnAddClicked(ItemType.NOTE))
                },
            )
        }
    }
}

@Composable
private fun SelectionFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            MiniFabAction(
                icon = Icons.Outlined.Archive,
                label = stringResource(R.string.home_selection_archive),
                onClick = onArchive,
            )
            Spacer(Modifier.height(8.dp))
            MiniFabAction(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.home_selection_delete),
                onClick = onDelete,
            )
            Spacer(Modifier.height(12.dp))
        }
        AppFab(
            icon = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.home_selection_actions),
            onClick = onToggle,
        )
    }
}

private data class HomeFabPreviewScenario(val homeMode: HomeMode, val expanded: Boolean = false)

private class HomeFabPreviewProvider : PreviewParameterProvider<HomeFabPreviewScenario> {
    override val values = sequenceOf(
        HomeFabPreviewScenario(HomeMode.Normal),
        HomeFabPreviewScenario(HomeMode.Normal, expanded = true),
        HomeFabPreviewScenario(HomeMode.Selection(setOf(1L, 2L))),
        HomeFabPreviewScenario(HomeMode.Selection(setOf(1L, 2L)), expanded = true),
    )
}

@PreviewLightDark
@Composable
private fun HomeFabPreview(@PreviewParameter(HomeFabPreviewProvider::class) scenario: HomeFabPreviewScenario) {
    UdsTheme {
        Surface {
            HomeFab(
                visible = true,
                homeMode = scenario.homeMode,
                addMenuExpanded = scenario.expanded,
                onAddMenuExpandedChange = {},
                onEvent = {},
            )
        }
    }
}
