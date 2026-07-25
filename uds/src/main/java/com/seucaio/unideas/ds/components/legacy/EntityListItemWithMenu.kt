package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * List row for a simple named entity (section/tag) with a trailing overflow menu
 * offering rename/delete — the manage-list pattern shared by Sections and Tags.
 */
@Composable
fun EntityListItemWithMenu(
    title: String,
    optionsContentDescription: String,
    renameLabel: String,
    deleteLabel: String,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    UnideasListItem(
        title = title,
        modifier = modifier,
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = optionsContentDescription)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(renameLabel) },
                        onClick = {
                            menuExpanded = false
                            onRenameClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(deleteLabel) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        },
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun EntityListItemWithMenuPreview() {
    UdsTheme {
        Surface {
            EntityListItemWithMenu(
                title = "Trabalho",
                optionsContentDescription = "Opções",
                renameLabel = "Renomear",
                deleteLabel = "Excluir",
                onRenameClick = {},
                onDeleteClick = {},
            )
        }
    }
}
