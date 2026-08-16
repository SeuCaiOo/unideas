package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun MarkdownPreviewToggle(
    isPreviewMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
            contentDescription = stringResource(R.string.item_form_markdown_preview_toggle),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp),
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
