package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
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
    onFormatClick: (MarkdownFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        FormatButton(
            icon = Icons.Default.FormatBold,
            contentDescription = stringResource(R.string.item_form_markdown_bold),
            onClick = { onFormatClick(MarkdownFormat.BOLD) },
        )
        FormatButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = stringResource(R.string.item_form_markdown_italic),
            onClick = { onFormatClick(MarkdownFormat.ITALIC) },
        )
        FormatButton(
            icon = Icons.Default.FormatStrikethrough,
            contentDescription = stringResource(R.string.item_form_markdown_strikethrough),
            onClick = { onFormatClick(MarkdownFormat.STRIKETHROUGH) },
        )
        FormatButton(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            contentDescription = stringResource(R.string.item_form_markdown_bullet_list),
            onClick = { onFormatClick(MarkdownFormat.BULLET_LIST) },
        )
        FormatButton(
            icon = Icons.Default.FormatListNumbered,
            contentDescription = stringResource(R.string.item_form_markdown_numbered_list),
            onClick = { onFormatClick(MarkdownFormat.NUMBERED_LIST) },
        )
        FormatButton(
            icon = Icons.Default.Checklist,
            contentDescription = stringResource(R.string.item_form_markdown_checklist),
            onClick = { onFormatClick(MarkdownFormat.CHECKLIST) },
        )
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@PreviewLightDark
@Composable
private fun MarkdownToolbarPreview() {
    UdsTheme {
        Surface {
            MarkdownToolbar(onFormatClick = {})
        }
    }
}
