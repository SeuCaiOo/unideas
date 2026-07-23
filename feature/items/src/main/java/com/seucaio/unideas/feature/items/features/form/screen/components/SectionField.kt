package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.ds.components.inputs.DropdownField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun SectionField(
    availableSections: List<Section>,
    sectionId: Long?,
    onSectionChanged: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownField(
        options = availableSections.map { it.name },
        selected = availableSections.firstOrNull { it.id == sectionId }?.name.orEmpty(),
        emptyOptionLabel = stringResource(R.string.item_form_section_none),
        onSelect = { name ->
            onSectionChanged(availableSections.firstOrNull { it.name == name }?.id)
        },
        modifier = modifier,
    )
}

private val previewSections =
    listOf(Section(id = 1L, name = "Trabalho"), Section(id = 2L, name = "Casa"))

@PreviewLightDark
@Composable
private fun SectionFieldPreview() {
    UdsTheme {
        Surface {
            SectionField(
                availableSections = previewSections,
                sectionId = 1L,
                onSectionChanged = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
