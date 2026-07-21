package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.screen.HomePreviewProvider
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState
import com.seucaio.unideas.feature.home.features.panel.viewmodel.ItemSectionGroup

/**
 * Sentinel standing in for [ItemSectionGroup.sectionId] `null` (unsectioned bucket) in the local
 * collapse-state sets of [ItemsListContent]/[ItemsGridContent] — Room section IDs are always
 * positive.
 */
internal const val NO_SECTION_KEY = -1L

/**
 * Display mode for [ItemsContent] — a **presentation-only** switch (via
 * [com.seucaio.unideas.ds.components.buttons.ViewModeToggleButton] at the call site), does not
 * touch grouping/filtering. [LIST] and [GRID] are equal siblings — the user picks one, neither
 * replaces the other.
 */
internal enum class ItemsViewMode { LIST, GRID }

/**
 * Home's tab-items content: the one empty-state check both view modes need, then dispatch by
 * [viewMode] to [ItemsListContent] or [ItemsGridContent] — each owns its own layout/grouping
 * rendering (including per-item spacing — the caller doesn't pass padding down), this only
 * decides which one runs. Shared between
 * [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen] and
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen`. [footer], if present,
 * renders after the last item/group in either mode — a plain `@Composable`, each child adapts it
 * to its own `LazyColumn`/`LazyVerticalGrid` internally.
 */
@Composable
internal fun ItemsContent(
    state: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewMode: ItemsViewMode = ItemsViewMode.LIST,
    footer: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (state.tabItems.isEmpty()) {
            val emptyMessageRes = if (state.hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding
            UnideasEmptyContent(messageRes = emptyMessageRes, modifier = Modifier.fillMaxSize())
        } else if (viewMode == ItemsViewMode.GRID) {
            ItemsGridContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                footer = footer,
            )
        } else {
            ItemsListContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                footer = footer,
            )
        }
    }
}

/**
 * Unfiltered — unlike [ItemsListPreviewProvider]/[ItemsGridPreviewProvider], includes the empty
 * states too: [ItemsContent] is the only one of the three that still renders them (it routes
 * empty [HomeUiState.Success.tabItems] away from both children before they ever see it).
 */
internal class ItemsContentPreviewProvider : PreviewParameterProvider<HomeUiState.Success> {
    override val values = HomePreviewProvider().values.filterIsInstance<HomeUiState.Success>()
}

@PreviewLightDark
@Composable
private fun ItemsContentListPreview(
    @PreviewParameter(ItemsContentPreviewProvider::class) state: HomeUiState.Success,
) {
    UdsTheme {
        Surface {
            ItemsContent(state = state, onEvent = {}, viewMode = ItemsViewMode.LIST)
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsContentGridPreview(
    @PreviewParameter(ItemsContentPreviewProvider::class) state: HomeUiState.Success,
) {
    UdsTheme {
        Surface {
            ItemsContent(state = state, onEvent = {}, viewMode = ItemsViewMode.GRID)
        }
    }
}
