package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.ds.components.inputs.DropdownField
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent

@Composable
fun SectionFieldV2(
    availableSections: List<Section>,
    sectionId: Long?,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormField(label = stringResource(R.string.item_form_section_label), modifier = modifier) {
        DropdownField(
            options = availableSections.map { it.name },
            selected = availableSections.firstOrNull { it.id == sectionId }?.name.orEmpty(),
            emptyOptionLabel = stringResource(R.string.item_form_section_none),
            onSelect = { name ->
                val newSectionId = availableSections.firstOrNull { it.name == name }?.id
                onEvent(ItemFormEvent.OnSectionChanged(newSectionId))
            },
        )
    }
}
