package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.ds.components.chips.SelectableChipRow
import com.seucaio.unideas.ds.components.chips.SelectableChipUi
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent

@Composable
fun TagsFieldV2(
    availableTags: List<Tag>,
    selectedTagIds: Set<Long>,
    onEvent: (ItemFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormField(label = stringResource(R.string.item_form_tags_label), modifier = modifier) {
        SelectableChipRow(
            chips = availableTags.map { tag ->
                SelectableChipUi(id = tag.id.toString(), label = tag.name, selected = tag.id in selectedTagIds)
            },
            onToggle = { id -> onEvent(ItemFormEvent.OnTagToggled(id.toLong())) },
        )
    }
}
