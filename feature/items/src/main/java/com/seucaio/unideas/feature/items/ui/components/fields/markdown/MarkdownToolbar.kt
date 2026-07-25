package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun MarkdownToolbar(
    isPreviewMode: Boolean,
    onFormatClick: (MarkdownFormat) -> Unit,
    onPreviewToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        FormatButton(
            icon = Icons.Default.FormatBold,
            contentDescription = stringResource(R.string.item_form_markdown_bold),
            enabled = !isPreviewMode,
            onClick = { onFormatClick(MarkdownFormat.BOLD) },
        )
        FormatButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = stringResource(R.string.item_form_markdown_italic),
            enabled = !isPreviewMode,
            onClick = { onFormatClick(MarkdownFormat.ITALIC) },
        )
        FormatButton(
            icon = Icons.Default.FormatStrikethrough,
            contentDescription = stringResource(R.string.item_form_markdown_strikethrough),
            enabled = !isPreviewMode,
            onClick = { onFormatClick(MarkdownFormat.STRIKETHROUGH) },
        )
        FormatButton(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            contentDescription = stringResource(R.string.item_form_markdown_bullet_list),
            enabled = !isPreviewMode,
            onClick = { onFormatClick(MarkdownFormat.BULLET_LIST) },
        )
        FormatButton(
            icon = Icons.Default.FormatListNumbered,
            contentDescription = stringResource(R.string.item_form_markdown_numbered_list),
            enabled = !isPreviewMode,
            onClick = { onFormatClick(MarkdownFormat.NUMBERED_LIST) },
        )
        FormatButton(
            icon = Icons.Default.Checklist,
            contentDescription = stringResource(R.string.item_form_markdown_checklist),
            enabled = !isPreviewMode,
            onClick = { onFormatClick(MarkdownFormat.CHECKLIST) },
        )
        IconButton(onClick = onPreviewToggle) {
            Icon(
                if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                contentDescription = stringResource(R.string.item_form_markdown_preview_toggle),
            )
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@PreviewLightDark
@Composable
private fun MarkdownToolbarEditingPreview() {
    UdsTheme {
        Surface {
            MarkdownToolbar(isPreviewMode = false, onFormatClick = {}, onPreviewToggle = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun MarkdownToolbarPreviewModePreview() {
    UdsTheme {
        Surface {
            MarkdownToolbar(isPreviewMode = true, onFormatClick = {}, onPreviewToggle = {})
        }
    }
}
