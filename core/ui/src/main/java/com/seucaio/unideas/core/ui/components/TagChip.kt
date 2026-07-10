package com.seucaio.unideas.core.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/** Generic label chip — takes a plain [label], not `domain.Tag` (`:core:ui` doesn't depend on `:domain`). */
@Composable
fun TagChip(label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    AssistChip(
        onClick = { onClick?.invoke() },
        label = { Text(label) },
        modifier = modifier,
        enabled = onClick != null,
    )
}

@Preview
@Composable
private fun TagChipPreview() {
    UnideasTheme {
        TagChip(label = "urgente", onClick = {})
    }
}
