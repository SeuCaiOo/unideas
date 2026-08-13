package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionBottomSheet(
    title: String,
    options: List<T>,
    selectedOption: T?,
    optionLabel: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    description: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        SelectionSheetContent(
            title = title,
            description = description,
            options = options,
            selectedOption = selectedOption,
            optionLabel = optionLabel,
            onOptionSelected = onOptionSelected,
        )
    }
}

@Composable
fun <T> SelectionSheetContent(
    title: String,
    description: String?,
    options: List<T>,
    selectedOption: T?,
    optionLabel: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = if (description == null) 8.dp else 0.dp),
        )

        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Column(modifier = Modifier.selectableGroup()) {
            options.forEachIndexed { index, option ->
                SelectionOptionRow(
                    label = optionLabel(option),
                    selected = option == selectedOption,
                    onSelect = { onOptionSelected(option) },
                )
                if (index < options.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SelectionOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

private class SelectionSheetDescriptionPreviewProvider : PreviewParameterProvider<String?> {
    override val values: Sequence<String?> = sequenceOf(null, "Choose how often this task should come back.")
}

@PreviewLightDark
@Composable
private fun SelectionSheetContentPreview(
    @PreviewParameter(SelectionSheetDescriptionPreviewProvider::class) description: String?,
) {
    UdsTheme {
        Surface {
            SelectionSheetContent(
                title = "Repeat",
                description = description,
                options = listOf("Daily", "Weekly", "Monthly"),
                selectedOption = "Weekly",
                optionLabel = { it },
                onOptionSelected = {},
            )
        }
    }
}
