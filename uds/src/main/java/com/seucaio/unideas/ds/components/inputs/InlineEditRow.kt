package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Row-in-place editing for an existing entity's name — [AddEntryRow] plus the local text state
 * every caller needs around it (seeded from [initialValue], reset whenever [key] changes, e.g.
 * a different row's id). Domain-agnostic: the manage-list "rename this row inline" pattern
 * shared by any screen with a name-only entity (e.g. Sections/Tags), not just one feature module.
 */
@Composable
fun InlineEditRow(
    key: Any,
    initialValue: String,
    placeholder: String,
    confirmContentDescription: String,
    cancelContentDescription: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(key) { mutableStateOf(initialValue) }
    AddEntryRow(
        value = value,
        onValueChange = { value = it },
        placeholder = placeholder,
        addContentDescription = confirmContentDescription,
        onSubmit = { if (value.isNotBlank()) onConfirm(value) },
        modifier = modifier,
        onCancel = onCancel,
        cancelContentDescription = cancelContentDescription,
    )
}

@PreviewLightDark
@Composable
private fun InlineEditRowPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            InlineEditRow(
                key = 1L,
                initialValue = "Work",
                placeholder = "Section name",
                confirmContentDescription = "Confirm",
                cancelContentDescription = "Cancel",
                onConfirm = {},
                onCancel = {},
            )
        }
    }
}
