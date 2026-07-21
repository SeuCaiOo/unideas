package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.ds.components.buttons.ViewModeToggleButton
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsViewMode

/**
 * [Filters] plus the list/grid [ViewModeToggleButton], side by side — shared between
 * [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen] and
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen` (same pairing as
 * [Filters]/[ItemsListContent]/[TasksNotesTabRow] already are), so a UI element used by both
 * screens has exactly one call site instead of being duplicated inline in each
 * (`docs/CONVENTIONS.md` § Screen (Compose)). [viewMode] stays hoisted at the screen — this
 * composable only computes the flip and reports it via [onViewModeChange].
 */
@Composable
internal fun ItemsFiltersBar(
    sections: List<Section>,
    tags: List<Tag>,
    sectionFilter: Long?,
    tagFilters: Set<Long>,
    onSectionFilterChange: (Long?) -> Unit,
    onTagFilterToggle: (Long) -> Unit,
    viewMode: ItemsViewMode,
    onViewModeChange: (ItemsViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Filters(
            sections = sections,
            tags = tags,
            sectionFilter = sectionFilter,
            tagFilters = tagFilters,
            onSectionFilterChange = onSectionFilterChange,
            onTagFilterToggle = onTagFilterToggle,
            modifier = Modifier.weight(1f),
        )
        ViewModeToggleButton(
            isGrid = viewMode == ItemsViewMode.GRID,
            onToggle = {
                onViewModeChange(if (viewMode == ItemsViewMode.GRID) ItemsViewMode.LIST else ItemsViewMode.GRID)
            },
            contentDescription = stringResource(
                if (viewMode == ItemsViewMode.GRID) {
                    R.string.home_view_mode_switch_to_list
                } else {
                    R.string.home_view_mode_switch_to_grid
                }
            ),
            modifier = Modifier.padding(end = 12.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun ItemsFiltersBarPreview() {
    UdsTheme {
        Surface {
            ItemsFiltersBar(
                sections = listOf(Section(id = 1L, name = "Trabalho"), Section(id = 2L, name = "Casa")),
                tags = listOf(Tag(id = 1L, name = "urgente")),
                sectionFilter = null,
                tagFilters = emptySet(),
                onSectionFilterChange = {},
                onTagFilterToggle = {},
                viewMode = ItemsViewMode.LIST,
                onViewModeChange = {},
            )
        }
    }
}
