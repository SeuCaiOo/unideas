package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.buttons.SegmentedControl
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun TypeSelectorField(type: ItemType, onTypeChanged: (ItemType) -> Unit, modifier: Modifier = Modifier) {
    val taskLabel = stringResource(R.string.item_form_type_task)
    val noteLabel = stringResource(R.string.item_form_type_note)
    SegmentedControl(
        options = listOf(taskLabel, noteLabel),
        selectedIndex = if (type == ItemType.TASK) 0 else 1,
        onSelect = { index -> onTypeChanged(if (index == 0) ItemType.TASK else ItemType.NOTE) },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun TypeSelectorFieldPreview() {
    UdsTheme {
        Surface {
            TypeSelectorField(type = ItemType.TASK, onTypeChanged = {})
        }
    }
}
