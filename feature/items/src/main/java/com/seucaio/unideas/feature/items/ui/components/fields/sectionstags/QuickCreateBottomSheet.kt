package com.seucaio.unideas.feature.items.ui.components.fields.sectionstags

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.inputs.AddEntryRow
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.screens.config.sectionstags.viewmodel.SectionsTagsDialogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateBottomSheet(
    target: SectionsTagsDialogState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        QuickCreateSheetContent(target = target, onConfirm = onConfirm)
    }
}

@Composable
fun QuickCreateSheetContent(
    target: SectionsTagsDialogState,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    val titleRes = if (target is SectionsTagsDialogState.AddSection) {
        R.string.item_config_add_section
    } else {
        R.string.item_config_add_tag
    }
    val placeholderRes = if (target is SectionsTagsDialogState.AddSection) {
        R.string.item_config_add_section_placeholder
    } else {
        R.string.item_config_add_tag_placeholder
    }

    Column(modifier.padding(bottom = 16.dp)) {
        Text(
            stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        AddEntryRow(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(placeholderRes),
            addContentDescription = stringResource(titleRes),
            onSubmit = {
                if (name.isNotBlank()) {
                    onConfirm(name)
                    name = ""
                }
            },
        )
    }
}

private class QuickCreateTargetPreviewProvider : PreviewParameterProvider<SectionsTagsDialogState> {
    override val values = sequenceOf(SectionsTagsDialogState.AddSection, SectionsTagsDialogState.AddTag)
}

@PreviewLightDark
@Composable
private fun QuickCreateSheetContentPreview(
    @PreviewParameter(QuickCreateTargetPreviewProvider::class) target: SectionsTagsDialogState,
) {
    UdsTheme {
        Surface {
            QuickCreateSheetContent(target = target, onConfirm = {})
        }
    }
}
