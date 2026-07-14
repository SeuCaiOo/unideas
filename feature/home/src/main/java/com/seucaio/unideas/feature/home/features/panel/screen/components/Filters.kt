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
import com.seucaio.unideas.core.ui.components.SectionDropdown
import com.seucaio.unideas.core.ui.components.TagChipRow
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.home.R

/**
 * Section (single-select dropdown) + tags (multi-select chips) filter row. Not Home-specific
 * in its own right (no [com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent]
 * dependency, plain callbacks instead), just the filter bar that happens to live on Home.
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
            SectionDropdown(
                options = sections.map { it.id to it.name },
                selectedId = sectionFilter,
                onSelect = onSectionFilterChange,
                noFilterLabel = stringResource(R.string.home_section_filter_none),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (tags.isNotEmpty()) {
            TagChipRow(
                tags = tags.map { it.id to it.name },
                selectedIds = tagFilters,
                onToggle = onTagFilterToggle,
                wrap = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}

private val previewSections = listOf(Section(id = 1L, name = "Trabalho"), Section(id = 2L, name = "Casa"))
private val previewTags = listOf(Tag(id = 1L, name = "urgente"), Tag(id = 2L, name = "pessoal"))

@PreviewLightDark
@Composable
private fun FiltersPreview() {
    UnideasTheme {
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
