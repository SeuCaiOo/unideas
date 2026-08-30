package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteConfirmBottomSheet(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    noteRequired: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (note: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        NoteConfirmSheetContent(
            titleRes = titleRes,
            messageRes = messageRes,
            noteRequired = noteRequired,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
fun NoteConfirmSheetContent(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    noteRequired: Boolean,
    onConfirm: (note: String?) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    var note by remember { mutableStateOf("") }
    val confirmEnabled = !noteRequired || note.isNotBlank()
    val noteOrNull = note.ifBlank { null }

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(messageRes), style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = {
                val labelRes = if (noteRequired) {
                    R.string.item_detail_note_required_label
                } else {
                    R.string.item_detail_note_optional_label
                }
                Text(stringResource(labelRes))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (confirmEnabled) { onConfirm(noteOrNull) } },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onConfirm(noteOrNull) },
            enabled = confirmEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(android.R.string.ok))
        }

        OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(android.R.string.cancel))
        }
    }
}

private data class NoteConfirmSheetPreviewData(
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
    val noteRequired: Boolean,
)

private class NoteConfirmSheetPreviewProvider : PreviewParameterProvider<NoteConfirmSheetPreviewData> {

    override val values: Sequence<NoteConfirmSheetPreviewData> = sequenceOf(
        NoteConfirmSheetPreviewData(
            titleRes = R.string.item_detail_complete_confirm_title,
            messageRes = R.string.item_detail_complete_confirm_message,
            noteRequired = false,
        ),
        NoteConfirmSheetPreviewData(
            titleRes = R.string.item_detail_complete_late_confirm_title,
            messageRes = R.string.item_detail_complete_late_confirm_message,
            noteRequired = true,
        ),
    )
}

@PreviewLightDark
@Composable
private fun NoteConfirmSheetContentPreview(
    @PreviewParameter(NoteConfirmSheetPreviewProvider::class) previewData: NoteConfirmSheetPreviewData,
) {
    UdsTheme {
        Surface {
            NoteConfirmSheetContent(
                titleRes = previewData.titleRes,
                messageRes = previewData.messageRes,
                noteRequired = previewData.noteRequired,
                onConfirm = {},
            )
        }
    }
}
