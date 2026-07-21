package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.ds.components.chips.SelectableChipRow
import com.seucaio.unideas.ds.components.chips.SelectableChipUi
import com.seucaio.unideas.ds.components.inputs.FilterDropdownPill
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R

/**
 * Rendered via `:uds`'s native `FilterDropdownPill` + `SelectableChipRow`. Both `:uds` pieces
 * are domain-agnostic (`String`/[SelectableChipUi]); the [Section]/[Tag] name<->id bridging
 * stays here, in the feature that owns those domain types.
 */
@Composable
fun Filters(
    sections: List<Section>,
    tags: List<Tag>,
    sectionFilter: Long?,
    tagFilters: Set<Long>,
    onSectionFilterChange: (Long?) -> Unit,
    onTagFilterToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (sections.isNotEmpty()) {
            val selectedName = sections.firstOrNull { it.id == sectionFilter }?.name.orEmpty()
            FilterDropdownPill(
                options = sections.map { it.name },
                selected = selectedName,
                allOptionLabel = stringResource(R.string.home_section_filter_none),
                onSelect = { name -> onSectionFilterChange(sections.firstOrNull { it.name == name }?.id) },
            )
        }
        if (tags.isNotEmpty()) {
            SelectableChipRow(
                chips = tags.map { tag ->
                    SelectableChipUi(id = tag.id.toString(), label = tag.name, selected = tag.id in tagFilters)
                },
                onToggle = { id -> onTagFilterToggle(id.toLong()) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private val previewSections = listOf(Section(id = 1L, name = "Trabalho"), Section(id = 2L, name = "Casa"))
private val previewTags = listOf(Tag(id = 1L, name = "urgente"), Tag(id = 2L, name = "pessoal"))

@PreviewLightDark
@Composable
private fun FiltersPreview() {
    UdsTheme {
        Surface {
            Filters(
                sections = previewSections,
                tags = previewTags,
                sectionFilter = null,
                tagFilters = setOf(1L),
                onSectionFilterChange = {},
                onTagFilterToggle = {},
            )
        }
    }
}
