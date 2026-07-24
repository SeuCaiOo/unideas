package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader
import com.seucaio.unideas.ds.components.lists.GroupHeader
import com.seucaio.unideas.ds.components.lists.ListItemCard
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.pinnedContainerColor
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemSectionGroup
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsState

/** Column count for [ItemsGridContent]'s grid. */
private const val ITEMS_GRID_COLUMNS = 2

/** Everything a rendered [ItemSectionGroup] needs besides the group itself — shared across all groups in one grid. */
private data class GridGroupRenderContext(
    val showHeaders: Boolean,
    val noSectionLabel: String,
    val checkContentDescription: String,
    val collapsedKeys: Set<Long>,
    val onToggleCollapse: (Long) -> Unit,
    val onEvent: (HomeEvent) -> Unit,
)

/**
 * Home's tab-items **grid** — [ItemsViewMode.GRID] sibling of [ItemsListContent], same grouping/
 * collapse behavior, [ListItemCard] cells instead of a plain row (that doesn't fit a half-width
 * cell — its title has nowhere to go, confirmed on-device). Called from [ItemsContent]; assumes
 * [ItemsState.tabItems] is non-empty, [ItemsContent] already handles the empty state.
 *
 * When [sectionFilter] is `null`, renders [ItemsState.groupedTabItems] the same way as
 * [ItemsListContent] — pinned Sections' groups first, under an emphasized "Pinned" [GroupHeader]
 * divider (indented further, so they read as nested under it), then the rest with no divider at
 * all, each spanning both columns. Collapse state is local UI-only state (not in the ViewModel —
 * purely cosmetic, no business logic, nothing to test at the VM level per `mvi.md`). [footer], if
 * present, renders as a full-width row after the last group — same content type as
 * [ItemsListContent]'s, adapted here to a spanning grid item instead of a plain list row.
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
    val pinnedLabel = stringResource(R.string.home_group_pinned)
    val showHeaders = sectionFilter == null

    var collapsedKeys by remember { mutableStateOf(emptySet<Long>()) }
    val context = GridGroupRenderContext(
        showHeaders = showHeaders,
        noSectionLabel = noSectionLabel,
        checkContentDescription = checkContentDescription,
        collapsedKeys = collapsedKeys,
        onToggleCollapse = { key ->
            collapsedKeys = if (key in collapsedKeys) collapsedKeys - key else collapsedKeys + key
        },
        onEvent = onEvent,
    )

    val pinnedGroups = if (showHeaders) itemsState.groupedTabItems.filter { it.isPinned } else emptyList()
    val otherGroups = if (showHeaders) {
        itemsState.groupedTabItems.filterNot { it.isPinned }
    } else {
        itemsState.groupedTabItems
    }

    LazyVerticalGrid(columns = GridCells.Fixed(ITEMS_GRID_COLUMNS), modifier = modifier) {
        if (pinnedGroups.isNotEmpty()) {
            item(key = "meta-pinned", span = { GridItemSpan(ITEMS_GRID_COLUMNS) }) {
                GroupHeader(pinnedLabel, emphasized = true)
            }
            pinnedGroups.forEach { group -> sectionGroup(group, context, indentStart = PINNED_INDENT) }
        }
        otherGroups.forEach { group -> sectionGroup(group, context) }
        if (footer != null) {
            item(span = { GridItemSpan(ITEMS_GRID_COLUMNS) }) { footer() }
        }
    }
}

private fun LazyGridScope.sectionGroup(
    group: ItemSectionGroup,
    context: GridGroupRenderContext,
    indentStart: Dp = 0.dp,
) {
    val key = group.sectionId ?: NO_SECTION_KEY
    val expanded = key !in context.collapsedKeys

    if (context.showHeaders) {
        item(key = "group-$key", span = { GridItemSpan(ITEMS_GRID_COLUMNS) }) {
            CollapsibleGroupHeader(
                title = group.sectionName ?: context.noSectionLabel,
                itemCount = group.items.size,
                expanded = expanded,
                onToggle = { context.onToggleCollapse(key) },
                isPinned = group.isPinned,
                onTogglePin = group.sectionId?.let { sectionId ->
                    {
                        context.onEvent(HomeEvent.OnSectionPinToggled(sectionId, !group.isPinned))
                    }
                },
                indentStart = indentStart,
            )
        }
    }
    if (!context.showHeaders || expanded) {
        items(group.items, key = { it.id }) { item ->
            ListItemCard(
                ui = item.toListItemUi(context.checkContentDescription),
                onClick = { context.onEvent(HomeEvent.OnItemClicked(item.id)) },
                onToggleCheck = { context.onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                containerColor = pinnedContainerColor(group.isPinned, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(8.dp),
            )
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
