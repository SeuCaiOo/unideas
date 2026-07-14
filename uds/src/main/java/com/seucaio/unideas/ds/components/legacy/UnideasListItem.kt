package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Generic list row — no dividers between items (project convention); use vertical
 * spacing (8–12dp) between rows in the list instead.
 */
@Composable
fun UnideasListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.let {
            it()
        }
        Column(modifier = Modifier.weight(1f).widthIn(min = 0.dp).padding(horizontal = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailingContent?.let {
            it()
        }
    }
}

@PreviewLightDark
@Composable
private fun UnideasListItemPreview() {
    UdsTheme {
        Surface {
            UnideasListItem(title = "Pagar contas", subtitle = "Vence em 2 dias")
        }
    }
}

@PreviewLightDark
@Composable
private fun UnideasListItemMinimalPreview() {
    UdsTheme {
        Surface {
            UnideasListItem(title = "Ideia de projeto")
        }
    }
}
