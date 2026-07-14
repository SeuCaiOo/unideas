package com.seucaio.unideas.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/**
 * Wraps a `Scaffold`'s `floatingActionButton` slot, showing [content] only when [visible] —
 * the "no FAB while loading/erroring, nothing to act on yet" convention shared by every list
 * screen (Home, ItemsList, Sections, Tags). [content] stays a slot (not a fixed icon/onClick)
 * so screens whose FAB opens a menu (Home's add-task/note dropdown) still fit.
 */
@Composable
fun ConditionalFab(visible: Boolean, content: @Composable () -> Unit) {
    if (visible) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun ConditionalFabVisiblePreview() {
    UnideasTheme {
        Surface {
            ConditionalFab(visible = true) {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    }
}
