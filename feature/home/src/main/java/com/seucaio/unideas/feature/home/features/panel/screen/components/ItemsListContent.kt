package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader
import com.seucaio.unideas.ds.components.lists.ListContent
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemSectionGroup

/**
 * Home's tab-items **list** — on top of `:uds`'s generic [ListContent], maps [Item] to
 * [com.seucaio.unideas.ds.components.lists.ListItemUi]/dispatches [HomeEvent]. Called from
 * [ItemsContent] when [ItemsViewMode.LIST] is active — assumes
 * [HomeUiState.Success.tabItems] is non-empty, [ItemsContent] already handles the empty state.
 * [ItemsGridContent] is the [ItemsViewMode.GRID] sibling.
 *
 * When [HomeUiState.Success.sectionFilter] is `null`, renders [HomeUiState.Success.groupedTabItems]
 * instead — a header per Section, collapsible, ahead of that group's rows. Collapse state is
 * local UI-only state (not in the ViewModel — purely cosmetic, no business logic, nothing to test
 * at the VM level per `mvi.md`). [footer], if present, renders as the list's last row — a plain
 * `@Composable` so callers (and [ItemsContent]) don't need to know this renders on a `LazyColumn`
 * (as opposed to [ItemsGridContent]'s `LazyVerticalGrid`).
 */
@Composable
internal fun ItemsListContent(
    state: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    val checkContentDescription = stringResource(R.string.home_item_recurring_content_description)
    val noSectionLabel = stringResource(R.string.home_group_no_section)

    if (state.sectionFilter == null) {
        GroupedItemsList(
            groups = state.groupedTabItems,
            noSectionLabel = noSectionLabel,
            checkContentDescription = checkContentDescription,
            onEvent = onEvent,
            modifier = modifier,
            footer = footer,
        )
    } else {
        ListContent(
            items = state.tabItems,
            key = { it.id },
            emptyContent = {
                // Unreachable in practice — ItemsContent already routes empty tabItems away from
                // here — kept because ListContent's emptyContent param isn't nullable.
                UnideasEmptyContent(
                    messageRes = if (state.hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding,
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

    LazyColumn(modifier = modifier) {
        groups.forEach { group ->
            val key = group.sectionId ?: NO_SECTION_KEY
            val expanded = key !in collapsedKeys

            item(key = "group-$key") {
                CollapsibleGroupHeader(
                    title = group.sectionName ?: noSectionLabel,
                    itemCount = group.items.size,
                    expanded = expanded,
                    onToggle = {
                        collapsedKeys = if (expanded) collapsedKeys + key else collapsedKeys - key
                    },
                )
            }
            if (expanded) {
                items(group.items, key = { it.id }) { item ->
                    ListItemRow(
                        ui = item.toListItemUi(checkContentDescription),
                        onClick = { onEvent(HomeEvent.OnItemClicked(item.id)) },
                        onToggleCheck = { onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
        if (footer != null) {
            item { footer() }
        }
    }
}

internal class ItemsListPreviewProvider : PreviewParameterProvider<HomeUiState.Success> {
    override val values = HomePreviewProvider().values
        .filterIsInstance<HomeUiState.Success>()
        .filter { it.tabItems.isNotEmpty() }
}

@PreviewLightDark
@Composable
private fun ItemsListContentPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) state: HomeUiState.Success,
) {
    UdsTheme {
        Surface {
            ItemsListContent(state = state, onEvent = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsListContentWithFooterPreview(
    @PreviewParameter(ItemsListPreviewProvider::class) state: HomeUiState.Success,
) {
    UdsTheme {
        Surface {
            ItemsListContent(state = state, onEvent = {}) {
                NavRow(
                    icon = Icons.AutoMirrored.Outlined.List,
                    label = "View all items",
                    onClick = {},
                )
            }
        }
    }
}
