package com.seucaio.unideas.feature.home.features.home.screen.components.items

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
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader
import com.seucaio.unideas.ds.components.lists.GroupHeader
import com.seucaio.unideas.ds.components.lists.ListContent
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.components.lists.item.ListItemRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.pinnedContainerColor
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode
import com.seucaio.unideas.feature.home.features.home.viewmodel.ItemSectionGroup

@Composable
internal fun ItemsListContent(
    itemsState: HomeItemsState,
    sectionFilter: Long?,
    hasAnyItem: Boolean,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    homeMode: HomeMode = HomeMode.Normal,
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
            homeMode = homeMode,
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
                    ui = item.toListItemUi(checkContentDescription, homeMode),
                    onClick = { onEvent(HomeEvent.OnItemClicked(item.id)) },
                    onLongClick = { onEvent(HomeEvent.OnItemLongPressed(item.id)) },
                    onToggleCheck = { onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                    onToggleSelection = { onEvent(HomeEvent.OnItemSelectionToggled(item.id)) },
                    onTogglePin = { onEvent(HomeEvent.OnItemPinToggled(item.id, !item.isPinned)) },
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
    val pinnedItemsLabel: String,
    val checkContentDescription: String,
    val expandedOverrides: Map<Long, Boolean>,
    val onToggleCollapse: (key: Long, defaultExpanded: Boolean) -> Unit,
    val onEvent: (HomeEvent) -> Unit,
    val homeMode: HomeMode,
)

@Composable
private fun GroupedItemsList(
    groups: List<ItemSectionGroup>,
    noSectionLabel: String,
    checkContentDescription: String,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    homeMode: HomeMode = HomeMode.Normal,
    footer: (@Composable () -> Unit)? = null,
) {
    var expandedOverrides by remember { mutableStateOf(emptyMap<Long, Boolean>()) }
    val pinnedGroups = groups.filter { it.isPinned }
    val otherGroups = groups.filterNot { it.isPinned }
    val pinnedLabel = stringResource(R.string.home_group_pinned)
    val context = GroupRenderContext(
        noSectionLabel = noSectionLabel,
        pinnedItemsLabel = stringResource(R.string.home_group_pinned_items),
        checkContentDescription = checkContentDescription,
        expandedOverrides = expandedOverrides,
        onToggleCollapse = { key, defaultExpanded ->
            val currentlyExpanded = expandedOverrides[key] ?: defaultExpanded
            expandedOverrides = expandedOverrides + (key to !currentlyExpanded)
        },
        onEvent = onEvent,
        homeMode = homeMode,
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
    val key = if (group.isPinnedItemsGroup) PINNED_ITEMS_GROUP_KEY else group.sectionId ?: NO_SECTION_KEY
    val expanded = context.expandedOverrides[key] ?: true

    item(key = "group-$key") {
        val homeMode = context.homeMode
        CollapsibleGroupHeader(
            title = if (group.isPinnedItemsGroup) context.pinnedItemsLabel else group.sectionName ?: context.noSectionLabel,
            itemCount = group.items.size,
            expanded = expanded,
            onToggle = { context.onToggleCollapse(key, true) },
            isPinned = group.isPinned,
            onTogglePin = group.sectionId?.let { sectionId ->
                {
                    context.onEvent(HomeEvent.OnSectionPinToggled(sectionId, !group.isPinned))
                }
            },
            indentStart = indentStart,
            isSelected = when (homeMode) {
                is HomeMode.Selection -> group.items.all { it.id in homeMode.selectedItemIds }
                HomeMode.Normal -> null
            },
            onToggleSelection = { context.onEvent(HomeEvent.OnGroupSelectAllClicked(group.sectionId)) },
        )
    }
    if (expanded) {
        items(group.items, key = { it.id }) { item ->
            ListItemRow(
                ui = item.toListItemUi(context.checkContentDescription, context.homeMode),
                onClick = { context.onEvent(HomeEvent.OnItemClicked(item.id)) },
                onLongClick = { context.onEvent(HomeEvent.OnItemLongPressed(item.id)) },
                onToggleCheck = { context.onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                onToggleSelection = { context.onEvent(HomeEvent.OnItemSelectionToggled(item.id)) },
                onTogglePin = { context.onEvent(HomeEvent.OnItemPinToggled(item.id, !item.isPinned)) },
                containerColor = pinnedContainerColor(group.isPinned, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

internal data class ItemsListPreviewScenario(
    val itemsState: HomeItemsState,
    val homeMode: HomeMode = HomeMode.Normal,
)

internal class ItemsListPreviewProvider : PreviewParameterProvider<ItemsListPreviewScenario> {
    private val itemsStates = HomePreviewProvider().values
        .map { it.itemsState }
        .filter { it.tabItems.isNotEmpty() }
        .toList()

    override val values: Sequence<ItemsListPreviewScenario> = itemsStates
        .map { ItemsListPreviewScenario(it) }
        .plus(
            ItemsListPreviewScenario(
                itemsState = itemsStates.first(),
                homeMode = HomeMode.Selection(itemsStates.first().groupedTabItems.first().items.map { it.id }.toSet(),),
            ),
        )
        .asSequence()
}

@PreviewLightDark
@Composable
private fun ItemsListContentPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) scenario: ItemsListPreviewScenario,
) {
    UdsTheme {
        Surface {
            ItemsListContent(
                itemsState = scenario.itemsState,
                sectionFilter = null,
                hasAnyItem = true,
                onEvent = {},
                homeMode = scenario.homeMode,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsListContentWithFooterPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) scenario: ItemsListPreviewScenario,
) {
    UdsTheme {
        Surface {
            ItemsListContent(
                itemsState = scenario.itemsState,
                sectionFilter = null,
                hasAnyItem = true,
                onEvent = {},
                homeMode = scenario.homeMode,
            ) {
                NavRow(
                    icon = Icons.AutoMirrored.Outlined.List,
                    label = "View all items",
                    onClick = {},
                )
            }
        }
    }
}
