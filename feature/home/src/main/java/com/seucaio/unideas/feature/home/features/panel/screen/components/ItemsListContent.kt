package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.components.lists.ListContent
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeUiState

/**
 * V2 (#84): home's tab-items list, on top of `:uds`'s generic [ListContent] — maps [Item] to
 * [com.seucaio.unideas.ds.components.lists.ListItemUi]/dispatches [HomeEvent], leaving list-shape
 * concerns (empty/list/footer) to [ListContent]. Shared between
 * [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreenV2] and
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen`.
 */
@Composable
internal fun ItemsListContent(
    state: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    footer: (LazyListScope.() -> Unit)? = null,
) {
    val checkContentDescription = stringResource(R.string.home_item_recurring_content_description)
    val emptyMessageRes = if (state.hasAnyItem) R.string.home_tab_empty else R.string.home_empty_onboarding

    ListContent(
        items = state.tabItems,
        key = { it.id },
        emptyContent = { UnideasEmptyContent(messageRes = emptyMessageRes, modifier = modifier.fillMaxSize()) },
        itemContent = { item ->
            ListItemRow(
                ui = item.toListItemUi(checkContentDescription),
                onClick = { onEvent(HomeEvent.OnItemClicked(item.id)) },
                onToggleCheck = { onEvent(HomeEvent.OnCompleteClicked(item.id)) },
                modifier = itemModifier,
            )
        },
        modifier = modifier,
        footer = footer,
    )
}
