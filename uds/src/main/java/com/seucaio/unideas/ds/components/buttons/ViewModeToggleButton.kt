package com.seucaio.unideas.ds.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Two-state icon button toggling between a list and a grid presentation of the same content —
 * shows the icon for the mode a tap would switch **to** (grid icon while in list mode, and vice
 * versa), same convention as a play/pause toggle. Caller owns which mode is active; this is
 * display-only, no view-mode concept lives in `:uds`.
 */
@Composable
fun ViewModeToggleButton(
    isGrid: Boolean,
    onToggle: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            if (isGrid) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView,
            contentDescription = contentDescription,
        )
    }
}

@PreviewLightDark
@Composable
private fun ViewModeToggleButtonListPreview() {
    UdsTheme {
        Surface {
            ViewModeToggleButton(isGrid = false, onToggle = {}, contentDescription = "Switch to grid view")
        }
    }
}

@PreviewLightDark
@Composable
private fun ViewModeToggleButtonGridPreview() {
    UdsTheme {
        Surface {
            ViewModeToggleButton(isGrid = true, onToggle = {}, contentDescription = "Switch to list view")
        }
    }
}
