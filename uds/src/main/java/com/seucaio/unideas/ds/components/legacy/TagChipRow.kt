package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Row of [TagChip]s for a `(id, label)` list — the "iterate tags, render a chip" pattern
 * shared by the item form, item detail, and Home's filter bar. [selectedIds]/[onToggle]
 * left `null` renders read-only chips (item detail); passing both makes each chip a
 * selectable toggle (item form, Home filters). [wrap] switches between a horizontally
 * scrolling [LazyRow] (form/detail — compact, single line) and a wrapping [FlowRow]
 * (Home's filter bar, where a scrollable single line isn't as discoverable).
 */
@Composable
fun TagChipRow(
    tags: List<Pair<Long, String>>,
    modifier: Modifier = Modifier,
    selectedIds: Set<Long>? = null,
    onToggle: ((Long) -> Unit)? = null,
    wrap: Boolean = false,
) {
    if (wrap) {
        FlowRow(modifier = modifier) {
            tags.forEach { (id, label) ->
                TagChip(
                    label = label,
                    selected = selectedIds?.contains(id) == true,
                    onClick = onToggle?.let { { it(id) } },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    } else {
        LazyRow(modifier = modifier) {
            items(tags, key = { it.first }) { (id, label) ->
                TagChip(
                    label = label,
                    selected = selectedIds?.contains(id) == true,
                    onClick = onToggle?.let { { it(id) } },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}

private val previewTags = listOf(1L to "urgente", 2L to "pessoal")

@PreviewLightDark
@Composable
private fun TagChipRowSelectablePreview() {
    UdsTheme {
        Surface {
            TagChipRow(tags = previewTags, selectedIds = setOf(1L), onToggle = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun TagChipRowReadOnlyPreview() {
    UdsTheme {
        Surface {
            TagChipRow(tags = previewTags)
        }
    }
}
