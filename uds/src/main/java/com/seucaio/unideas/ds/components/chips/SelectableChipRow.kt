package com.seucaio.unideas.ds.components.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

data class SelectableChipUi(val id: String, val label: String, val selected: Boolean)

@Composable
fun SelectableChipRow(chips: List<SelectableChipUi>, onToggle: (String) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            SelectableChip(
                label = chip.label,
                selected = chip.selected,
                onClick = { onToggle(chip.id) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SelectableChipRowPreview() {
    UdsTheme {
        Surface {
            SelectableChipRow(
                chips = listOf(
                    SelectableChipUi(id = "1", label = "urgente", selected = true),
                    SelectableChipUi(id = "2", label = "pessoal", selected = false),
                ),
                onToggle = {},
            )
        }
    }
}
