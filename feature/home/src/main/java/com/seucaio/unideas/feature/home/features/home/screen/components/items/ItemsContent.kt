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

@Composable
internal fun ItemsContent(
    itemsState: HomeItemsState,
    sectionFilter: Long?,
    hasAnyItem: Boolean,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    homeMode: HomeMode = HomeMode.Normal,
    footer: (@Composable () -> Unit)? = null,
) {
    ItemsOrEmptyContent(itemsState, hasAnyItem, modifier) {
        ItemsListContent(
            itemsState = itemsState,
            sectionFilter = sectionFilter,
            hasAnyItem = hasAnyItem,
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize(),
            homeMode = homeMode,
            footer = footer,
        )
    }
}

@Deprecated("Avoid — grid layout, don't extend or use.")
@Composable
internal fun ItemsContentViewMode(
    itemsState: HomeItemsState,
    filterState: FilterState,
    hasAnyItem: Boolean,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    homeMode: HomeMode = HomeMode.Normal,
    footer: (@Composable () -> Unit)? = null,
) {
    ItemsOrEmptyContent(itemsState, hasAnyItem, modifier) {
        if (filterState.viewMode == ItemsViewMode.GRID) {
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

@Composable
private fun ItemsOrEmptyContent(
    itemsState: HomeItemsState,
    hasAnyItem: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (itemsState.tabItems.isEmpty()) {
            val emptyMessageRes = if (hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding
            UnideasEmptyContent(messageRes = emptyMessageRes, modifier = Modifier.fillMaxSize())
        } else {
            content()
        }
    }
}

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
private fun ItemsContentViewModeListPreview(
    @PreviewParameter(ItemsContentPreviewProvider::class) scenario: ItemsContentPreviewScenario,
) {
    UdsTheme {
        Surface {
            ItemsContentViewMode(
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
private fun ItemsContentViewModeGridPreview(
    @PreviewParameter(ItemsContentPreviewProvider::class) scenario: ItemsContentPreviewScenario,
) {
    UdsTheme {
        Surface {
            ItemsContentViewMode(
                itemsState = scenario.fixture.itemsState,
                filterState = scenario.fixture.filterState.copy(viewMode = ItemsViewMode.GRID),
                hasAnyItem = scenario.fixture.hasAnyItem,
                onEvent = {},
                homeMode = scenario.homeMode,
            )
        }
    }
}
