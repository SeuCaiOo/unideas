package com.seucaio.unideas.ds.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * App-bar style icon button. [containerColor]/[shape] default to transparent/circle (plain
 * icon, Material ripple only); pass a filled [containerColor] (e.g. `colorScheme.primary`) +
 * [shape] (e.g. [Radii.MiniFab]'s rounded square) for a filled variant — e.g. `AddEntryRow`'s
 * confirm button reuses this instead of hand-rolling its own `Box`.
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = Color.Transparent,
    shape: Shape = CircleShape,
    buttonSize: Dp = 44.dp,
    iconSize: Dp = 23.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        shape = shape,
        colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@PreviewLightDark
@Composable
private fun AppIconButtonPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            AppIconButton(icon = Icons.Outlined.Settings, contentDescription = "Settings", onClick = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun AppIconButtonFilledPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            AppIconButton(
                icon = Icons.Outlined.Add,
                contentDescription = "Add",
                onClick = {},
                tint = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(Radii.MiniFab),
            )
        }
    }
}
