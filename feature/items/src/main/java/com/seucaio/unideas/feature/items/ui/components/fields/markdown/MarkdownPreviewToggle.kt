package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun MarkdownPreviewToggle(
    isPreviewMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
            contentDescription = stringResource(R.string.item_form_markdown_preview_toggle),
        )
    }
}

@PreviewLightDark
@Composable
private fun MarkdownPreviewToggleEditingPreview() {
    UdsTheme {
        Surface {
            MarkdownPreviewToggle(isPreviewMode = false, onClick = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun MarkdownPreviewTogglePreviewModePreview() {
    UdsTheme {
        Surface {
            MarkdownPreviewToggle(isPreviewMode = true, onClick = {})
        }
    }
}
