package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import com.seucaio.unideas.ds.components.chips.SelectableChip
import com.seucaio.unideas.ds.components.inputs.FilterDropdownPill
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R

/**
 * V2 (#84) of [Filters] — same contract, rendered via `:uds`'s native `FilterDropdownPill` +
 * `SelectableChip` instead of the legacy `SectionDropdown`/`TagChipRow`. `FilterDropdownPill`
 * selects by name (portable module, no domain `Long` ids), so this maps name back to id.
 */
@Composable
fun FiltersV2(
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
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    SelectableChip(
                        label = tag.name,
                        selected = tag.id in tagFilters,
                        onClick = { onTagFilterToggle(tag.id) },
                    )
                }
            }
        }
    }
}

private val previewSections = listOf(Section(id = 1L, name = "Trabalho"), Section(id = 2L, name = "Casa"))
private val previewTags = listOf(Tag(id = 1L, name = "urgente"), Tag(id = 2L, name = "pessoal"))

@PreviewLightDark
@Composable
private fun FiltersV2Preview() {
    UdsTheme {
        Surface {
            FiltersV2(
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
