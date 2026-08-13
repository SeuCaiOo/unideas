package com.seucaio.unideas.ds.components.lists.item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
internal fun ListItemCheckbox(
    checked: Boolean,
    contentDescription: String,
    onToggle: () -> Unit,
    size: Dp = 22.dp,
    iconSize: Dp = 16.dp,
) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(Radii.Checkbox))
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                if (checked) 0.dp else 2.dp,
                LocalUdsExtendedColors.current.textTertiary,
                RoundedCornerShape(Radii.Checkbox),
            )
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ListItemCheckboxPreview() {
    UdsTheme {
        Row(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ListItemCheckbox(checked = false, contentDescription = "Confirm", onToggle = {})
            ListItemCheckbox(checked = true, contentDescription = "Confirm", onToggle = {})
            ListItemCheckbox(
                checked = true,
                contentDescription = "Confirm",
                onToggle = {},
                size = 20.dp,
                iconSize = 14.dp,
            )
        }
    }
}
