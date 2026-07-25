package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Generic list body: empty state, or a [LazyColumn] of [items] rendered via [itemContent] (e.g.
 * [ListItemRow]) plus an optional trailing [footer] (e.g. a "browse more" [NavRow]). Domain-agnostic
 * counterpart to [ListItemRow] — that renders one row, this renders the whole list around it.
 */
@Composable
fun <T> ListContent(
    items: List<T>,
    key: (T) -> Any,
    emptyContent: @Composable () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    footer: (LazyListScope.() -> Unit)? = null,
) {
    if (items.isEmpty()) {
        emptyContent()
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items, key = key) { item -> itemContent(item) }
            footer?.invoke(this)
        }
    }
}
