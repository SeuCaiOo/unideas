package com.seucaio.unideas.feature.items.features.form.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.ds.components.chips.SelectableChipRow
import com.seucaio.unideas.ds.components.chips.SelectableChipUi
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun TagsField(
    availableTags: List<Tag>,
    selectedTagIds: Set<Long>,
    onTagToggled: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormField(label = stringResource(R.string.item_form_tags_label), modifier = modifier) {
        SelectableChipRow(
            chips = availableTags.map { tag ->
                SelectableChipUi(id = tag.id.toString(), label = tag.name, selected = tag.id in selectedTagIds)
            },
            onToggle = { id -> onTagToggled(id.toLong()) },
        )
    }
}

private val previewTags = listOf(Tag(id = 1L, name = "urgente"), Tag(id = 2L, name = "pessoal"))

@PreviewLightDark
@Composable
private fun TagsFieldPreview() {
    UdsTheme {
        Surface {
            TagsField(
                availableTags = previewTags,
                selectedTagIds = setOf(1L),
                onTagToggled = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
