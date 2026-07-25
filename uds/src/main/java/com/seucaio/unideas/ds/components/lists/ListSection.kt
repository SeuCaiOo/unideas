package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * A titled group of rows: [GroupHeader] followed by [content]. Pairs a section's title with its
 * items so screens (Settings, ...) don't repeat the header + rows scaffolding for every section.
 */
@Composable
fun ListSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier) {
        GroupHeader(title)
        content()
    }
}

@PreviewLightDark
@Composable
private fun ListSectionPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            ListSection(title = "Organize") {
                NavRow(icon = Icons.Outlined.Folder, label = "Sections", onClick = {})
                NavRow(icon = Icons.Outlined.CloudUpload, label = "Backup", onClick = {})
            }
        }
    }
}
