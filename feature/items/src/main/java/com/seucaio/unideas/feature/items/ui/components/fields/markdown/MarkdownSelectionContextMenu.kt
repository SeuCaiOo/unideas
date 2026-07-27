package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.seucaio.unideas.feature.items.R

/**
 * Appends Markdown formatting actions (negrito/itálico/riscado/lista/numerada/checklist) to the
 * text selection context menu — the same native menu that already shows Copy/Cut/Paste/Select all
 * (WhatsApp-style: select text, open the menu, pick a format). A second, selection-driven way to
 * reach the same formats as [MarkdownToolbar] — doesn't replace it.
 */
@Composable
fun Modifier.markdownFormatContextMenuItems(onFormatClick: (MarkdownFormat) -> Unit): Modifier {
    val boldLabel = stringResource(R.string.item_form_markdown_bold)
    val italicLabel = stringResource(R.string.item_form_markdown_italic)
    val strikethroughLabel = stringResource(R.string.item_form_markdown_strikethrough)
    val bulletListLabel = stringResource(R.string.item_form_markdown_bullet_list)
    val numberedListLabel = stringResource(R.string.item_form_markdown_numbered_list)
    val checklistLabel = stringResource(R.string.item_form_markdown_checklist)

    return appendTextContextMenuComponents {
        separator()
        item(key = MarkdownFormat.BOLD, label = boldLabel) {
            onFormatClick(MarkdownFormat.BOLD)
            close()
        }
        item(key = MarkdownFormat.ITALIC, label = italicLabel) {
            onFormatClick(MarkdownFormat.ITALIC)
            close()
        }
        item(key = MarkdownFormat.STRIKETHROUGH, label = strikethroughLabel) {
            onFormatClick(MarkdownFormat.STRIKETHROUGH)
            close()
        }
        item(key = MarkdownFormat.BULLET_LIST, label = bulletListLabel) {
            onFormatClick(MarkdownFormat.BULLET_LIST)
            close()
        }
        item(key = MarkdownFormat.NUMBERED_LIST, label = numberedListLabel) {
            onFormatClick(MarkdownFormat.NUMBERED_LIST)
            close()
        }
        item(key = MarkdownFormat.CHECKLIST, label = checklistLabel) {
            onFormatClick(MarkdownFormat.CHECKLIST)
            close()
        }
    }
}
