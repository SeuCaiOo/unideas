package com.seucaio.unideas.feature.home.features.home.screen.components.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.screen.HomePreviewFixture
import com.seucaio.unideas.feature.home.features.home.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.home.viewmodel.FilterState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeItemsState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode
import com.seucaio.unideas.feature.home.features.home.viewmodel.ItemSectionGroup
import com.seucaio.unideas.feature.home.features.home.viewmodel.ItemsViewMode

/**
 * Sentinel standing in for [ItemSectionGroup.sectionId] `null` (unsectioned bucket) in the local
 * collapse-state sets of [ItemsListContent]/[ItemsGridContent] — Room section IDs are always
 * positive.
 */
internal const val NO_SECTION_KEY = -1L

/**
 * Extra start indent for a pinned group's [CollapsibleGroupHeader], nesting it under the
 * emphasized "Pinned" [GroupHeader] above it.
 */
internal val PINNED_INDENT = 12.dp

/**
 * Home's tab-items content: the one empty-state check both view modes need, then dispatch by
 * [FilterState.viewMode] to [ItemsListContent] or [ItemsGridContent] — each owns its own
 * layout/grouping rendering (including per-item spacing — the caller doesn't pass padding down),
 * this only decides which one runs. [footer], if present, renders after the last item/group in
 * either mode — a plain `@Composable`, each child adapts it to its own
 * `LazyColumn`/`LazyVerticalGrid` internally.
 */
@Composable
internal fun ItemsContent(
    itemsState: HomeItemsState,
    filterState: FilterState,
    hasAnyItem: Boolean,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    homeMode: HomeMode = HomeMode.Normal,
    footer: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (itemsState.tabItems.isEmpty()) {
            val emptyMessageRes = if (hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding
            UnideasEmptyContent(messageRes = emptyMessageRes, modifier = Modifier.fillMaxSize())
        } else if (filterState.viewMode == ItemsViewMode.GRID) {
            ItemsGridContent(
                itemsState = itemsState,
                sectionFilter = filterState.sectionFilter,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                homeMode = homeMode,
                footer = footer,
            )
        } else {
            ItemsListContent(
                itemsState = itemsState,
                sectionFilter = filterState.sectionFilter,
                hasAnyItem = hasAnyItem,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                homeMode = homeMode,
                footer = footer,
            )
        }
    }
}

/**
 * Unfiltered — unlike [ItemsListPreviewProvider]/[ItemsGridPreviewProvider], includes the empty
 * states too: [ItemsContent] is the only one of the three that still renders them (it routes an
 * empty [HomeItemsState.tabItems] away from both children before they ever see it).
 */
internal data class ItemsContentPreviewScenario(
    val fixture: HomePreviewFixture,
    val homeMode: HomeMode = HomeMode.Normal,
)

internal class ItemsContentPreviewProvider : PreviewParameterProvider<ItemsContentPreviewScenario> {
    private val fixtures = HomePreviewProvider().values.toList()

    override val values: Sequence<ItemsContentPreviewScenario> = fixtures
        .map { ItemsContentPreviewScenario(it) }
        .plus(
            ItemsContentPreviewScenario(
                fixture = fixtures.first { it.itemsState.tabItems.isNotEmpty() },
                homeMode = HomeMode.Selection(
                    fixtures.first { it.itemsState.tabItems.isNotEmpty() }
                        .itemsState.tabItems.take(2).map { it.id }.toSet(),
                ),
            ),
        )
        .asSequence()
}

@PreviewLightDark
@Composable
private fun ItemsContentListPreview(
    @PreviewParameter(ItemsContentPreviewProvider::class) scenario: ItemsContentPreviewScenario,
) {
    UdsTheme {
        Surface {
            ItemsContent(
                itemsState = scenario.fixture.itemsState,
                filterState = scenario.fixture.filterState.copy(viewMode = ItemsViewMode.LIST),
                hasAnyItem = scenario.fixture.hasAnyItem,
                onEvent = {},
                homeMode = scenario.homeMode,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsContentGridPreview(
    @PreviewParameter(ItemsContentPreviewProvider::class) scenario: ItemsContentPreviewScenario,
) {
    UdsTheme {
        Surface {
            ItemsContent(
                itemsState = scenario.fixture.itemsState,
                filterState = scenario.fixture.filterState.copy(viewMode = ItemsViewMode.GRID),
                hasAnyItem = scenario.fixture.hasAnyItem,
                onEvent = {},
                homeMode = scenario.homeMode,
            )
        }
    }
}
