package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.buttons.AppIconButton
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Text row for a single-field entry: create a new one, or [onCancel] non-null to also edit an
 * existing one in place (confirm icon swaps Add→Check once [value] has content either way —
 * same affordance, since "submit this text" is the same action whether adding or renaming).
 */
@Composable
fun AddEntryRow(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    addContentDescription: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    cancelContentDescription: String? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            onImeAction = onSubmit,
            modifier = Modifier.weight(1f)
        )
        if (onCancel != null) {
            AppIconButton(
                icon = Icons.Outlined.Close,
                contentDescription = cancelContentDescription.orEmpty(),
                onClick = onCancel,
                tint = LocalUdsExtendedColors.current.textTertiary,
            )
        }
        AppIconButton(
            icon = if (value.isBlank()) Icons.Outlined.Add else Icons.Outlined.Check,
            contentDescription = addContentDescription,
            onClick = onSubmit,
            tint = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(Radii.MiniFab),
        )
    }
}

@PreviewLightDark
@Composable
private fun AddEntryRowPreview() {
    UdsTheme {
        var text by remember { mutableStateOf("") }
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            AddEntryRow(value = text, onValueChange = {
                text = it
            }, placeholder = "New section...", addContentDescription = "Add", onSubmit = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun AddEntryRowEditingPreview() {
    UdsTheme {
        var text by remember { mutableStateOf("Work") }
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            AddEntryRow(
                value = text,
                onValueChange = { text = it },
                placeholder = "Section name",
                addContentDescription = "Confirm",
                onSubmit = {},
                onCancel = {},
                cancelContentDescription = "Cancel",
            )
        }
    }
}
