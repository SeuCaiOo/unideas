package com.seucaio.unideas.ds.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.DsTheme

/** App-bar style icon button; Material ripple stands in for the prototype's hover fill. */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    buttonSize: Dp = 44.dp,
    iconSize: Dp = 23.dp
) {
    IconButton(onClick = onClick, modifier = modifier.size(buttonSize)) {
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
    DsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            AppIconButton(icon = Icons.Outlined.Settings, contentDescription = "Settings", onClick = {})
        }
    }
}
