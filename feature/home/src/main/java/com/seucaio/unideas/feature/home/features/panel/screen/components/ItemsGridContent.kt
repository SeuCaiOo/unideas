package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader
import com.seucaio.unideas.ds.components.lists.ListItemCard
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsState

/** Column count for [ItemsGridContent]'s grid. */
private const val ITEMS_GRID_COLUMNS = 2

/**
 * Home's tab-items **grid** — [ItemsViewMode.GRID] sibling of [ItemsListContent], same grouping/
 * collapse behavior, [ListItemCard] cells instead of [ListItemRow] (that doesn't fit a half-width
 * cell — its title has nowhere to go, confirmed on-device). Called from [ItemsContent]; assumes
 * [ItemsState.tabItems] is non-empty, [ItemsContent] already handles the empty state.
 *
 * When [sectionFilter] is `null`, renders [ItemsState.groupedTabItems] with a header per Section,
 * collapsible, spanning both columns — same as [ItemsListContent]. Collapse state is local
 * UI-only state (not in the ViewModel — purely cosmetic, no business logic, nothing to test at the
 * VM level per `mvi.md`). [footer], if present, renders as a full-width row after the last group —
 * same content type as [ItemsListContent]'s, adapted here to a spanning grid item instead of a
 * plain list row.
 */
@Composable
internal fun ItemsGridContent(
    itemsState: ItemsState,
    sectionFilter: Long?,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    val checkContentDescription = stringResource(R.string.home_item_recurring_content_description)
    val noSectionLabel = stringResource(R.string.home_group_no_section)
    val showHeaders = sectionFilter == null

    var collapsedKeys by remember { mutableStateOf(emptySet<Long>()) }

    LazyVerticalGrid(columns = GridCells.Fixed(ITEMS_GRID_COLUMNS), modifier = modifier) {
        itemsState.groupedTabItems.forEach { group ->
            val key = group.sectionId ?: NO_SECTION_KEY
            val expanded = key !in collapsedKeys

            if (showHeaders) {
                item(key = "group-$key", span = { GridItemSpan(ITEMS_GRID_COLUMNS) }) {
                    CollapsibleGroupHeader(
                        title = group.sectionName ?: noSectionLabel,
                        itemCount = group.items.size,
                        expanded = expanded,
                        onToggle = {
                            collapsedKeys = if (expanded) collapsedKeys + key else collapsedKeys - key
                        },
                    )
                }
            }
            if (!showHeaders || expanded) {
                items(group.items, key = { it.id }) { item ->
                    ListItemCard(
                        ui = item.toListItemUi(checkContentDescription),
                        onClick = { onEvent(HomeEvent.OnItemClicked(item.id)) },
                        onToggleCheck = { onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
        if (footer != null) {
            item(span = { GridItemSpan(ITEMS_GRID_COLUMNS) }) { footer() }
        }
    }
}

internal class ItemsGridPreviewProvider : PreviewParameterProvider<ItemsState> {
    override val values = HomePreviewProvider().values
        .map { it.itemsState }
        .filter { it.tabItems.isNotEmpty() }
}

@PreviewLightDark
@Composable
private fun ItemsGridContentPreview(
    @PreviewParameter(ItemsGridPreviewProvider::class) itemsState: ItemsState,
) {
    UdsTheme {
        Surface {
            ItemsGridContent(itemsState = itemsState, sectionFilter = null, onEvent = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsGridContentWithFooterPreview(
    @PreviewParameter(ItemsGridPreviewProvider::class) itemsState: ItemsState,
) {
    UdsTheme {
        Surface {
            ItemsGridContent(itemsState = itemsState, sectionFilter = null, onEvent = {}) {
                NavRow(
                    icon = Icons.AutoMirrored.Outlined.List,
                    label = "View all items",
                    onClick = {},
                )
            }
        }
    }
}
