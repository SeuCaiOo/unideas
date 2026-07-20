package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.buttons.SegmentedControl
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent

@Composable
fun TypeSelectorFieldV2(type: ItemType, onEvent: (ItemFormEvent) -> Unit, modifier: Modifier = Modifier) {
    val taskLabel = stringResource(R.string.item_form_type_task)
    val noteLabel = stringResource(R.string.item_form_type_note)
    SegmentedControl(
        options = listOf(taskLabel, noteLabel),
        selectedIndex = if (type == ItemType.TASK) 0 else 1,
        onSelect = { index ->
            onEvent(ItemFormEvent.OnTypeChanged(if (index == 0) ItemType.TASK else ItemType.NOTE))
        },
        modifier = modifier,
    )
}
