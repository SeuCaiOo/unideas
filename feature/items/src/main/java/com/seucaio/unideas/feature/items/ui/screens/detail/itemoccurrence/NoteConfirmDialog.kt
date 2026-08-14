package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun NoteConfirmDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    noteRequired: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (note: String?) -> Unit,
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column {
                Text(stringResource(messageRes))
                NoteTextField(
                    note = note,
                    onNoteChanged = { note = it },
                    noteRequired = noteRequired,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(note.ifBlank { null }) },
                enabled = !noteRequired || note.isNotBlank(),
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun NoteTextField(
    note: String,
    onNoteChanged: (String) -> Unit,
    noteRequired: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChanged,
        label = {
            val labelRes = if (noteRequired) {
                R.string.item_detail_note_required_label
            } else {
                R.string.item_detail_note_optional_label
            }
            Text(stringResource(labelRes))
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun NoteConfirmDialogContent(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    noteRequired: Boolean,
    modifier: Modifier = Modifier,
) {
    var note by remember { mutableStateOf("") }

    Card(modifier = modifier.padding(24.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(messageRes))
                NoteTextField(
                    note = note,
                    onNoteChanged = { note = it },
                    noteRequired = noteRequired,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = {}) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(onClick = {}, enabled = !noteRequired || note.isNotBlank()) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}

private data class NoteConfirmDialogPreviewData(
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
    val noteRequired: Boolean,
)

private class NoteConfirmDialogPreviewProvider : PreviewParameterProvider<NoteConfirmDialogPreviewData> {

    override val values: Sequence<NoteConfirmDialogPreviewData> = sequenceOf(
        NoteConfirmDialogPreviewData(
            titleRes = R.string.item_detail_complete_confirm_title,
            messageRes = R.string.item_detail_complete_confirm_message,
            noteRequired = false,
        ),
        NoteConfirmDialogPreviewData(
            titleRes = R.string.item_detail_complete_late_confirm_title,
            messageRes = R.string.item_detail_complete_late_confirm_message,
            noteRequired = true,
        ),
    )
}

@PreviewLightDark
@Composable
private fun NoteConfirmDialogPreview(
    @PreviewParameter(NoteConfirmDialogPreviewProvider::class) previewData: NoteConfirmDialogPreviewData,
) {
    UdsTheme {
        Surface {
            NoteConfirmDialogContent(
                titleRes = previewData.titleRes,
                messageRes = previewData.messageRes,
                noteRequired = previewData.noteRequired,
            )
        }
    }
}
