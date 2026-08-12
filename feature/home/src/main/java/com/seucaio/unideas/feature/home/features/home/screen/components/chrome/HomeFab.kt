package com.seucaio.unideas.feature.home.features.home.screen.components.chrome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.buttons.AppFab
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
            AppFab(
                icon = Icons.Default.Delete,
                contentDescription = stringResource(R.string.home_selection_delete),
                onClick = { onEvent(HomeEvent.OnDeleteSelectedClicked) },
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

private class HomeFabPreviewProvider : PreviewParameterProvider<HomeMode> {
    override val values = sequenceOf(HomeMode.Normal, HomeMode.Selection(setOf(1L, 2L)))
}

@PreviewLightDark
@Composable
private fun HomeFabPreview(@PreviewParameter(HomeFabPreviewProvider::class) homeMode: HomeMode) {
    UdsTheme {
        Surface {
            HomeFab(
                visible = true,
                homeMode = homeMode,
                addMenuExpanded = false,
                onAddMenuExpandedChange = {},
                onEvent = {},
            )
        }
    }
}
