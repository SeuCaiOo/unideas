package com.seucaio.unideas.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/**
 * Generic label chip — takes a plain [label], not `domain.Tag` (`:core:ui` doesn't depend on
 * `:domain`). [selected] drives the toggled-on visual, for multi-select tag pickers (item form,
 * Home filters).
 */
@Composable
fun TagChip(label: String, selected: Boolean = false, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    FilterChip(
        selected = selected,
        onClick = { onClick?.invoke() },
        label = { Text(label) },
        modifier = modifier,
        enabled = onClick != null,
    )
}

@PreviewLightDark
@Composable
private fun TagChipPreview() {
    UnideasTheme {
        Surface {
            Row(modifier = Modifier.padding(8.dp)) {
                TagChip(label = "urgente", selected = true, onClick = {})
                TagChip(label = "pessoal", selected = false, onClick = {})
            }
        }
    }
}
