package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * The title + optional subtitle every list-backed entity (task, note, or any future item type)
 * needs at the top of its detail screen — every such entity has a title, not all have a
 * subtitle/description. Wrapped in [SelectionContainer] so users can copy either line.
 */
@Composable
fun TitleSubtitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    SelectionContainer(modifier.fillMaxWidth()) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TitleSubtitlePreview() {
    UdsTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            Column {
                TitleSubtitle(title = "Pagar contas", subtitle = "Conta de luz e água")
                TitleSubtitle(title = "Ideia de projeto", modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}
