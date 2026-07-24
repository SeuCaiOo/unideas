package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader
import com.seucaio.unideas.ds.components.lists.GroupHeader
import com.seucaio.unideas.ds.components.lists.ListContent
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.pinnedContainerColor
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemSectionGroup
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemsState

/**
 * Home's tab-items **list** — on top of `:uds`'s generic [ListContent], maps [Item] to
 * [com.seucaio.unideas.ds.components.lists.ListItemUi]/dispatches [HomeEvent]. Called from
 * [ItemsContent] when [ItemsViewMode.LIST] is active — assumes [ItemsState.tabItems] is
 * non-empty, [ItemsContent] already handles the empty state. [ItemsGridContent] is the
 * [ItemsViewMode.GRID] sibling.
 *
 * When [sectionFilter] is `null`, renders [ItemsState.groupedTabItems] instead — pinned Sections'
 * groups first, under an emphasized "Pinned" [GroupHeader] divider (indented further than usual,
 * so they read as nested under it, not a sibling section at the same level), then the rest with no
 * divider at all — an "Others" label added no real information and, styled like a section header,
 * was mistaken for an empty, clickable section on first look. Each group keeps its own
 * collapsible header and rows. Collapse state is local UI-only state (not in the ViewModel —
 * purely cosmetic, no business logic, nothing to test at the VM level per `mvi.md`). [footer], if
 * present, renders as the list's last row — a plain `@Composable` so callers (and [ItemsContent])
 * don't need to know this renders on a `LazyColumn` (as opposed to [ItemsGridContent]'s
 * `LazyVerticalGrid`).
 */
@Composable
internal fun ItemsListContent(
    itemsState: ItemsState,
    sectionFilter: Long?,
    hasAnyItem: Boolean,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    val checkContentDescription = stringResource(R.string.home_item_recurring_content_description)
    val noSectionLabel = stringResource(R.string.home_group_no_section)

    if (sectionFilter == null) {
        GroupedItemsList(
            groups = itemsState.groupedTabItems,
            noSectionLabel = noSectionLabel,
            checkContentDescription = checkContentDescription,
            onEvent = onEvent,
            modifier = modifier,
            footer = footer,
        )
    } else {
        ListContent(
            items = itemsState.tabItems,
            key = { it.id },
            emptyContent = {
                // Unreachable in practice — ItemsContent already routes empty tabItems away from
                // here — kept because ListContent's emptyContent param isn't nullable.
                UnideasEmptyContent(
                    messageRes = if (hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            itemContent = { item ->
                ListItemRow(
                    ui = item.toListItemUi(checkContentDescription),
                    onClick = { onEvent(HomeEvent.OnItemClicked(item.id)) },
                    onToggleCheck = { onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            },
            modifier = modifier,
            footer = footer?.let { content -> { item { content() } } },
        )
    }
}

/** Everything a rendered [ItemSectionGroup] needs besides the group itself — shared across all groups in one list. */
private data class GroupRenderContext(
    val noSectionLabel: String,
    val checkContentDescription: String,
    val collapsedKeys: Set<Long>,
    val onToggleCollapse: (Long) -> Unit,
    val onEvent: (HomeEvent) -> Unit,
)

@Composable
private fun GroupedItemsList(
    groups: List<ItemSectionGroup>,
    noSectionLabel: String,
    checkContentDescription: String,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    var collapsedKeys by remember { mutableStateOf(emptySet<Long>()) }
    val pinnedGroups = groups.filter { it.isPinned }
    val otherGroups = groups.filterNot { it.isPinned }
    val pinnedLabel = stringResource(R.string.home_group_pinned)
    val context = GroupRenderContext(
        noSectionLabel = noSectionLabel,
        checkContentDescription = checkContentDescription,
        collapsedKeys = collapsedKeys,
        onToggleCollapse = { key ->
            collapsedKeys = if (key in collapsedKeys) collapsedKeys - key else collapsedKeys + key
        },
        onEvent = onEvent,
    )

    LazyColumn(modifier = modifier) {
        if (pinnedGroups.isNotEmpty()) {
            item(key = "meta-pinned") { GroupHeader(pinnedLabel, emphasized = true) }
            pinnedGroups.forEach { group -> sectionGroup(group, context, indentStart = PINNED_INDENT) }
        }
        otherGroups.forEach { group -> sectionGroup(group, context) }
        if (footer != null) {
            item { footer() }
        }
    }
}

private fun LazyListScope.sectionGroup(
    group: ItemSectionGroup,
    context: GroupRenderContext,
    indentStart: Dp = 0.dp,
) {
    val key = group.sectionId ?: NO_SECTION_KEY
    val expanded = key !in context.collapsedKeys

    item(key = "group-$key") {
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
    if (expanded) {
        items(group.items, key = { it.id }) { item ->
            ListItemRow(
                ui = item.toListItemUi(context.checkContentDescription),
                onClick = { context.onEvent(HomeEvent.OnItemClicked(item.id)) },
                onToggleCheck = { context.onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                containerColor = pinnedContainerColor(group.isPinned, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

internal class ItemsListPreviewProvider : PreviewParameterProvider<ItemsState> {
    override val values = HomePreviewProvider().values
        .map { it.itemsState }
        .filter { it.tabItems.isNotEmpty() }
}

@PreviewLightDark
@Composable
private fun ItemsListContentPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) itemsState: ItemsState,
) {
    UdsTheme {
        Surface {
            ItemsListContent(itemsState = itemsState, sectionFilter = null, hasAnyItem = true, onEvent = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsListContentWithFooterPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) itemsState: ItemsState,
) {
    UdsTheme {
        Surface {
            ItemsListContent(itemsState = itemsState, sectionFilter = null, hasAnyItem = true, onEvent = {}) {
                NavRow(
                    icon = Icons.AutoMirrored.Outlined.List,
                    label = "View all items",
                    onClick = {},
                )
            }
        }
    }
}
